package com.ecommerce.shipping.exception;

import com.ecommerce.shipping.entity.ShipmentStatus;

/**
 * Exception thrown when an invalid shipment status operation is attempted.
 * This includes invalid status transitions, operations not allowed in current status,
 * or other status-related business rule violations.
 */
public class InvalidShipmentStatusException extends RuntimeException {
    
    private final ShipmentStatus currentStatus;
    private final ShipmentStatus requestedStatus;
    private final String operation;
    
    /**
     * Constructor with message only
     * @param message the error message
     */
    public InvalidShipmentStatusException(String message) {
        super(message);
        this.currentStatus = null;
        this.requestedStatus = null;
        this.operation = null;
    }
    
    /**
     * Constructor with message and cause
     * @param message the error message
     * @param cause the underlying cause
     */
    public InvalidShipmentStatusException(String message, Throwable cause) {
        super(message, cause);
        this.currentStatus = null;
        this.requestedStatus = null;
        this.operation = null;
    }
    
    /**
     * Constructor for invalid status transition
     * @param message the error message
     * @param currentStatus the current status
     * @param requestedStatus the requested status
     */
    public InvalidShipmentStatusException(String message, ShipmentStatus currentStatus, ShipmentStatus requestedStatus) {
        super(message);
        this.currentStatus = currentStatus;
        this.requestedStatus = requestedStatus;
        this.operation = "status_transition";
    }
    
    /**
     * Constructor for invalid operation on current status
     * @param message the error message
     * @param currentStatus the current status
     * @param operation the operation that was attempted
     */
    public InvalidShipmentStatusException(String message, ShipmentStatus currentStatus, String operation) {
        super(message);
        this.currentStatus = currentStatus;
        this.requestedStatus = null;
        this.operation = operation;
    }
    
    /**
     * Static factory method for invalid status transition
     * @param currentStatus the current status
     * @param requestedStatus the requested status
     * @return InvalidShipmentStatusException instance
     */
    public static InvalidShipmentStatusException invalidTransition(ShipmentStatus currentStatus, ShipmentStatus requestedStatus) {
        String message = String.format(
            "Invalid status transition from %s to %s. This transition is not allowed.",
            currentStatus, requestedStatus
        );
        return new InvalidShipmentStatusException(message, currentStatus, requestedStatus);
    }
    
    /**
     * Static factory method for operation not allowed in current status
     * @param operation the operation that was attempted
     * @param currentStatus the current status
     * @return InvalidShipmentStatusException instance
     */
    public static InvalidShipmentStatusException operationNotAllowed(String operation, ShipmentStatus currentStatus) {
        String message = String.format(
            "Operation '%s' is not allowed when shipment status is %s",
            operation, currentStatus
        );
        return new InvalidShipmentStatusException(message, currentStatus, operation);
    }
    
    /**
     * Static factory method for cancellation not allowed
     * @param currentStatus the current status
     * @return InvalidShipmentStatusException instance
     */
    public static InvalidShipmentStatusException cancellationNotAllowed(ShipmentStatus currentStatus) {
        String message = String.format(
            "Shipment cannot be cancelled when status is %s. Cancellation is only allowed for shipments that are not yet delivered or already cancelled.",
            currentStatus
        );
        return new InvalidShipmentStatusException(message, currentStatus, "cancel");
    }
    
    /**
     * Static factory method for status update not allowed
     * @param currentStatus the current status
     * @param requestedStatus the requested status
     * @param reason additional reason for the restriction
     * @return InvalidShipmentStatusException instance
     */
    public static InvalidShipmentStatusException updateNotAllowed(ShipmentStatus currentStatus, 
                                                                 ShipmentStatus requestedStatus, 
                                                                 String reason) {
        String message = String.format(
            "Cannot update shipment status from %s to %s. Reason: %s",
            currentStatus, requestedStatus, reason
        );
        return new InvalidShipmentStatusException(message, currentStatus, requestedStatus);
    }
    
    /**
     * Static factory method for terminal status modification
     * @param currentStatus the current terminal status
     * @return InvalidShipmentStatusException instance
     */
    public static InvalidShipmentStatusException terminalStatusModification(ShipmentStatus currentStatus) {
        String message = String.format(
            "Cannot modify shipment with terminal status %s. Terminal statuses cannot be changed.",
            currentStatus
        );
        return new InvalidShipmentStatusException(message, currentStatus, "modify");
    }
    
    // Getters
    public ShipmentStatus getCurrentStatus() {
        return currentStatus;
    }
    
    public ShipmentStatus getRequestedStatus() {
        return requestedStatus;
    }
    
    public String getOperation() {
        return operation;
    }
    
    /**
     * Check if this exception is related to a status transition
     * @return true if this is a status transition error
     */
    public boolean isStatusTransitionError() {
        return "status_transition".equals(operation) && currentStatus != null && requestedStatus != null;
    }
    
    /**
     * Check if this exception is related to an operation restriction
     * @return true if this is an operation restriction error
     */
    public boolean isOperationRestrictionError() {
        return operation != null && !"status_transition".equals(operation) && currentStatus != null;
    }
    
    /**
     * Get a detailed error description
     * @return detailed error description
     */
    public String getDetailedDescription() {
        StringBuilder sb = new StringBuilder(getMessage());
        
        if (isStatusTransitionError()) {
            sb.append(" [Transition: ").append(currentStatus).append(" → ").append(requestedStatus).append("]");
        } else if (isOperationRestrictionError()) {
            sb.append(" [Operation: ").append(operation).append(", Current Status: ").append(currentStatus).append("]");
        }
        
        return sb.toString();
    }
}
