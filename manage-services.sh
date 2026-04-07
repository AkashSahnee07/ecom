#!/bin/bash

# E-commerce Microservices Management Script
# This script provides comprehensive service management capabilities

set -e

# Base directory (where this script lives)
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$BASE_DIR/logs"
PID_DIR="$BASE_DIR/pids"
mkdir -p "$LOG_DIR" "$PID_DIR"

# Java 17 home
export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || echo "$JAVA_HOME")

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Service definitions with ports
get_service_port() {
    case $1 in
        "eureka-server") echo "8761" ;;
        "config-server") echo "8888" ;;
        "api-gateway") echo "8080" ;;
        "user-service") echo "8081" ;;
        "product-service") echo "8082" ;;
        "cart-service") echo "8083" ;;
        "order-service") echo "8084" ;;
        "payment-service") echo "8085" ;;
        "inventory-service") echo "8086" ;;
        "recommendation-service") echo "8087" ;;
        "shipping-service") echo "8088" ;;
        "review-service") echo "8089" ;;
        "notification-service") echo "8090" ;;
        *) echo "" ;;
    esac
}

# Check if service exists
is_valid_service() {
    local service=$1
    case $service in
        "eureka-server"|"config-server"|"api-gateway"|"user-service"|"product-service"|"cart-service"|"order-service"|"payment-service"|"inventory-service"|"recommendation-service"|"shipping-service"|"review-service"|"notification-service")
            return 0 ;;
        *) return 1 ;;
    esac
}

# Service startup order
STARTUP_ORDER=(
    "eureka-server"
    "config-server"
    "api-gateway"
    "user-service"
    "product-service"
    "cart-service"
    "order-service"
    "payment-service"
    "inventory-service"
    "recommendation-service"
    "shipping-service"
    "review-service"
    "notification-service"
)

# Function to display usage
show_usage() {
    echo -e "${CYAN}E-commerce Microservices Management Script${NC}"
    echo -e "${CYAN}===========================================${NC}"
    echo ""
    echo -e "${BLUE}Usage: $0 [COMMAND] [OPTIONS]${NC}"
    echo ""
    echo -e "${YELLOW}Commands:${NC}"
    echo -e "  ${GREEN}status${NC}           Check status of all services"
    echo -e "  ${GREEN}start [service]${NC}  Start all services or a specific service"
    echo -e "  ${GREEN}stop [service]${NC}   Stop all services or a specific service"
    echo -e "  ${GREEN}restart [service]${NC} Restart all services or a specific service"
    echo -e "  ${GREEN}logs [service]${NC}   Show logs for a specific service"
    echo -e "  ${GREEN}health [service]${NC} Check health of a specific service"
    echo -e "  ${GREEN}list${NC}             List all available services"
    echo -e "  ${GREEN}clean${NC}            Clean up log files and PID files"
    echo -e "  ${GREEN}help${NC}             Show this help message"
    echo ""
    echo -e "${YELLOW}Examples:${NC}"
    echo -e "  $0 status                    # Check all services"
    echo -e "  $0 start                     # Start all services"
    echo -e "  $0 start user-service        # Start only user-service"
    echo -e "  $0 stop                      # Stop all services"
    echo -e "  $0 restart api-gateway       # Restart only api-gateway"
    echo -e "  $0 logs order-service        # Show logs for order-service"
    echo -e "  $0 health product-service    # Check health of product-service"
    echo ""
}

# Function to check if a service is running
is_service_running() {
    local service_name=$1
    local pid_file="$PID_DIR/$service_name.pid"

    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            return 0
        fi
    fi
    return 1
}

# Function to get service PID
get_service_pid() {
    local service_name=$1
    local pid_file="$PID_DIR/$service_name.pid"

    if [ -f "$pid_file" ]; then
        cat "$pid_file"
    else
        echo "N/A"
    fi
}

# Function to check service health
check_service_health() {
    local service_name=$1
    local port=$(get_service_port "$service_name")
    
    if [ -z "$port" ]; then
        echo -e "${RED}❌ Unknown service: $service_name${NC}"
        return 1
    fi
    
    if curl -s -f "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ $service_name is healthy (Port: $port)${NC}"
        return 0
    else
        echo -e "${RED}❌ $service_name is not healthy or not responding (Port: $port)${NC}"
        return 1
    fi
}

# Function to wait for service health
wait_for_health() {
    local service_name=$1
    local port=$(get_service_port "$service_name")
    local max_attempts=30
    local attempt=1

    echo -e "${YELLOW}⏳ Waiting for $service_name to be healthy...${NC}"

    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ $service_name is healthy!${NC}"
            return 0
        fi

        if [ $((attempt % 6)) -eq 0 ]; then
            echo -e "${YELLOW}   Attempt $attempt/$max_attempts - $service_name not ready yet...${NC}"
        fi
        sleep 5
        ((attempt++))
    done

    echo -e "${RED}❌ $service_name failed to start within expected time${NC}"
    return 1
}

# Function to start a service
start_service() {
    local service_name=$1
    local port=$(get_service_port "$service_name")
    
    if [ -z "$port" ]; then
        echo -e "${RED}❌ Unknown service: $service_name${NC}"
        return 1
    fi
    
    if is_service_running "$service_name"; then
        echo -e "${YELLOW}⚠️  $service_name is already running${NC}"
        return 0
    fi
    
    echo -e "${BLUE}🔄 Starting $service_name...${NC}"

    local jar_file="$BASE_DIR/$service_name/target/$service_name-1.0.0.jar"
    if [ ! -f "$jar_file" ]; then
        echo -e "${RED}❌ JAR not found: $jar_file (run '$0 compile' first)${NC}"
        return 1
    fi

    nohup "$JAVA_HOME/bin/java" -jar "$jar_file" > "$LOG_DIR/$service_name.log" 2>&1 &
    echo $! > "$PID_DIR/$service_name.pid"
    
    wait_for_health "$service_name"
}

# Function to stop a service
stop_service() {
    local service_name=$1
    local pid_file="$PID_DIR/$service_name.pid"

    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            echo -e "${YELLOW}🔄 Stopping $service_name (PID: $pid)...${NC}"
            kill $pid

            # Wait for process to stop
            local attempts=0
            while ps -p $pid > /dev/null 2>&1 && [ $attempts -lt 30 ]; do
                sleep 1
                ((attempts++))
            done

            if ps -p $pid > /dev/null 2>&1; then
                echo -e "${RED}⚠️  Force killing $service_name...${NC}"
                kill -9 $pid
            fi

            echo -e "${GREEN}✅ $service_name stopped${NC}"
        else
            echo -e "${YELLOW}⚠️  $service_name was not running${NC}"
        fi

        rm -f "$pid_file"
    else
        echo -e "${YELLOW}⚠️  No PID file found for $service_name${NC}"
    fi
}

# Function to show service status
show_status() {
    echo -e "${CYAN}📊 E-commerce Microservices Status${NC}"
    echo -e "${CYAN}===================================${NC}"
    echo ""
    
    local running_count=0
    local total_count=13
    
    printf "%-25s %-10s %-10s %-15s %s\n" "SERVICE" "STATUS" "PID" "PORT" "HEALTH"
    echo "---------------------------------------------------------------------------------"
    
    for service in "${STARTUP_ORDER[@]}"; do
        local port=$(get_service_port "$service")
        local pid=$(get_service_pid "$service")
        
        if is_service_running "$service"; then
            ((running_count++))
            local status="${GREEN}RUNNING${NC}"
            
            # Check health
            if curl -s -f "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
                local health="${GREEN}HEALTHY${NC}"
            else
                local health="${RED}UNHEALTHY${NC}"
            fi
        else
            local status="${RED}STOPPED${NC}"
            local health="${YELLOW}N/A${NC}"
            pid="N/A"
        fi
        
        printf "%-25s %-18s %-10s %-15s %s\n" "$service" "$status" "$pid" "$port" "$health"
    done
    
    echo ""
    echo -e "${BLUE}Summary: $running_count/$total_count services running${NC}"
    
    # Show infrastructure status
    echo ""
    echo -e "${CYAN}🐳 Infrastructure Services (Docker)${NC}"
    echo -e "${CYAN}===================================${NC}"
    if command -v docker-compose &> /dev/null && [ -f "docker-compose.yml" ]; then
        docker-compose ps
    else
        echo -e "${YELLOW}⚠️  Docker Compose not available or docker-compose.yml not found${NC}"
    fi
}

compile_all_services() {
    echo -e "${BLUE}🔨 Compiling all E-commerce Microservices...${NC}"
    echo -e "${BLUE}=============================================${NC}"

    # Build all services
    echo -e "${BLUE}🔨 Building all services (using JDK 17)...${NC}"
    if mvn -B clean install -DskipTests -f "$BASE_DIR/pom.xml"; then
        echo -e "${GREEN}✅ All services built successfully${NC}"
    else
        echo -e "${RED}❌ Build failed. Please check the build logs.${NC}"
        return 1
    fi
}

# Function to start all services
start_all_services() {
    echo -e "${BLUE}🚀 Starting all E-commerce Microservices...${NC}"
    echo -e "${BLUE}===========================================${NC}"

    # Start infrastructure services first
    echo -e "${BLUE}🐳 Starting infrastructure services...${NC}"
    if [ -f "$BASE_DIR/docker-compose.yml" ]; then
        docker-compose -f "$BASE_DIR/docker-compose.yml" up -d
        echo -e "${GREEN}✅ Infrastructure services started${NC}"
        echo -e "${YELLOW}⏳ Waiting for infrastructure to be ready...${NC}"
        sleep 15
    else
        echo -e "${YELLOW}⚠️  docker-compose.yml not found${NC}"
    fi

    # Start services in order
    echo -e "${BLUE}🚀 Starting microservices...${NC}"
    for service in "${STARTUP_ORDER[@]}"; do
        start_service "$service"
        sleep 2  # Brief pause between service starts
    done

    echo -e "${GREEN}🎉 All services started successfully!${NC}"
    show_service_urls
}

# Function to stop all services
stop_all_services() {
    echo -e "${BLUE}🛑 Stopping all E-commerce Microservices...${NC}"
    echo -e "${BLUE}===========================================${NC}"
    
    # Stop services in reverse order
    local reverse_order=()
    for ((i=${#STARTUP_ORDER[@]}-1; i>=0; i--)); do
        reverse_order+=("${STARTUP_ORDER[i]}")
    done
    
    for service in "${reverse_order[@]}"; do
        stop_service "$service"
    done
    
    # Stop infrastructure services
    echo -e "${BLUE}🐳 Stopping infrastructure services...${NC}"
    if [ -f "docker-compose.yml" ]; then
        docker-compose down
        echo -e "${GREEN}✅ Infrastructure services stopped${NC}"
    fi
    
    echo -e "${GREEN}✅ All services stopped successfully!${NC}"
}

# Function to restart all services
restart_all_services() {
    echo -e "${BLUE}🔄 Restarting all E-commerce Microservices...${NC}"
    echo -e "${BLUE}=============================================${NC}"
    
    stop_all_services
    sleep 5
    start_all_services
}

# Function to restart a specific service
restart_service() {
    local service_name=$1
    
    echo -e "${BLUE}🔄 Restarting $service_name...${NC}"
    stop_service "$service_name"
    sleep 2
    start_service "$service_name"
}

# Function to show service logs
show_logs() {
    local service_name=$1
    local log_file="$LOG_DIR/$service_name.log"

    if [ -f "$log_file" ]; then
        echo -e "${BLUE}📋 Showing logs for $service_name (last 50 lines)${NC}"
        echo -e "${BLUE}================================================${NC}"
        tail -50 "$log_file"
        echo ""
        echo -e "${YELLOW}💡 To follow logs in real-time: tail -f $log_file${NC}"
    else
        echo -e "${RED}❌ Log file not found: $log_file${NC}"
    fi
}

# Function to list all services
list_services() {
    echo -e "${CYAN}📋 Available Services${NC}"
    echo -e "${CYAN}====================${NC}"
    echo ""
    
    printf "%-25s %-10s %s\n" "SERVICE NAME" "PORT" "DESCRIPTION"
    echo "-------------------------------------------------------"
    printf "%-25s %-10s %s\n" "eureka-server" "8761" "Service Discovery"
    printf "%-25s %-10s %s\n" "config-server" "8888" "Configuration Management"
    printf "%-25s %-10s %s\n" "api-gateway" "8080" "API Gateway & Routing"
    printf "%-25s %-10s %s\n" "user-service" "8081" "User Management"
    printf "%-25s %-10s %s\n" "product-service" "8082" "Product Catalog"
    printf "%-25s %-10s %s\n" "cart-service" "8083" "Shopping Cart"
    printf "%-25s %-10s %s\n" "order-service" "8084" "Order Processing"
    printf "%-25s %-10s %s\n" "payment-service" "8085" "Payment Processing"
    printf "%-25s %-10s %s\n" "inventory-service" "8086" "Inventory Management"
    printf "%-25s %-10s %s\n" "recommendation-service" "8087" "Product Recommendations"
    printf "%-25s %-10s %s\n" "shipping-service" "8088" "Shipping & Delivery"
    printf "%-25s %-10s %s\n" "review-service" "8089" "Product Reviews"
    printf "%-25s %-10s %s\n" "notification-service" "8090" "Notifications"
    echo ""
}

# Function to clean up files
clean_up() {
    echo -e "${BLUE}🧹 Cleaning up files...${NC}"

    # Remove PID files
    if ls "$PID_DIR"/*.pid 1> /dev/null 2>&1; then
        echo -e "${YELLOW}🔄 Removing PID files...${NC}"
        rm -f "$PID_DIR"/*.pid
        echo -e "${GREEN}✅ PID files removed${NC}"
    fi

    # Ask about log files
    read -p "Do you want to remove log files? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        if ls "$LOG_DIR"/*.log 1> /dev/null 2>&1; then
            echo -e "${YELLOW}🔄 Removing log files...${NC}"
            rm -f "$LOG_DIR"/*.log
            echo -e "${GREEN}✅ Log files removed${NC}"
        else
            echo -e "${YELLOW}⚠️  No log files found${NC}"
        fi
    fi

    echo -e "${GREEN}✅ Cleanup completed${NC}"
}

# Function to show service URLs
show_service_urls() {
    echo ""
    echo -e "${BLUE}📊 Service URLs${NC}"
    echo -e "${BLUE}===============${NC}"
    echo "• Eureka Dashboard: http://localhost:8761"
    echo "• API Gateway: http://localhost:8080"
    echo "• Config Server: http://localhost:8888"
    echo "• Zipkin Tracing: http://localhost:9411"
    echo ""
    echo -e "${BLUE}📚 API Documentation${NC}"
    echo -e "${BLUE}====================${NC}"
    echo "• API Gateway Swagger: http://localhost:8080/swagger-ui.html"
    echo "• User Service: http://localhost:8081/swagger-ui.html"
    echo "• Product Service: http://localhost:8082/swagger-ui.html"
    echo "• Recommendation Service: http://localhost:8087/swagger-ui.html"
    echo "• Review Service: http://localhost:8089/swagger-ui.html"
    echo ""
}

# Main script logic
case "${1:-help}" in
    "status")
        show_status
        ;;
    "start")
        if [ -n "$2" ]; then
            if is_valid_service "$2"; then
                start_service "$2"
            else
                echo -e "${RED}❌ Unknown service: $2${NC}"
                echo -e "${YELLOW}💡 Use '$0 list' to see available services${NC}"
                exit 1
            fi
        else
            start_all_services
        fi
        ;;
      "compile")
              if [ -n "$2" ]; then
                  if is_valid_service "$2"; then
                      start_service "$2"
                  else
                      echo -e "${RED}❌ Unknown service: $2${NC}"
                      echo -e "${YELLOW}💡 Use '$0 list' to see available services${NC}"
                      exit 1
                  fi
              else
                  compile_all_services
              fi
              ;;
    "stop")
        if [ -n "$2" ]; then
            if is_valid_service "$2"; then
                stop_service "$2"
            else
                echo -e "${RED}❌ Unknown service: $2${NC}"
                echo -e "${YELLOW}💡 Use '$0 list' to see available services${NC}"
                exit 1
            fi
        else
            stop_all_services
        fi
        ;;
    "restart")
        if [ -n "$2" ]; then
            if is_valid_service "$2"; then
                restart_service "$2"
            else
                echo -e "${RED}❌ Unknown service: $2${NC}"
                echo -e "${YELLOW}💡 Use '$0 list' to see available services${NC}"
                exit 1
            fi
        else
            restart_all_services
        fi
        ;;
    "logs")
        if [ -n "$2" ]; then
            if is_valid_service "$2"; then
                show_logs "$2"
            else
                echo -e "${RED}❌ Unknown service: $2${NC}"
                echo -e "${YELLOW}💡 Use '$0 list' to see available services${NC}"
                exit 1
            fi
        else
            echo -e "${RED}❌ Please specify a service name${NC}"
            echo -e "${YELLOW}💡 Usage: $0 logs <service-name>${NC}"
            exit 1
        fi
        ;;
    "health")
        if [ -n "$2" ]; then
            if is_valid_service "$2"; then
                check_service_health "$2"
            else
                echo -e "${RED}❌ Unknown service: $2${NC}"
                echo -e "${YELLOW}💡 Use '$0 list' to see available services${NC}"
                exit 1
            fi
        else
            echo -e "${RED}❌ Please specify a service name${NC}"
            echo -e "${YELLOW}💡 Usage: $0 health <service-name>${NC}"
            exit 1
        fi
        ;;
    "list")
        list_services
        ;;
    "clean")
        clean_up
        ;;
    "help")
        show_usage
        ;;
    *)
        echo -e "${RED}❌ Unknown command: $1${NC}"
        echo ""
        show_usage
        exit 1
        ;;
esac