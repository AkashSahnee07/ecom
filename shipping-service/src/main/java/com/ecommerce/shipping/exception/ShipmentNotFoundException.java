package com.ecommerce.shipping.exception;

/**
 * Exception thrown when a shipment is not found in the system.
 * This is a runtime exception that indicates the requested shipment
 * does not exist in the database.
 */
public class ShipmentNotFoundException extends RuntimeException {
    
    private final String shipmentId;
    private final String trackingNumber;
    private final Long orderId;
    
    /**
     * Constructor with message only
     * @param message the error message
     */
    public ShipmentNotFoundException(String message) {
        super(message);
        this.shipmentId = null;
        this.trackingNumber = null;
        this.orderId = null;
    }
    
    /**
     * Constructor with message and cause
     * @param message the error message
     * @param cause the underlying cause
     */
    public ShipmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.shipmentId = null;
        this.trackingNumber = null;
        this.orderId = null;
    }
    
    /**
     * Constructor for shipment not found by ID
     * @param message the error message
     * @param shipmentId the shipment ID that was not found
     */
    public ShipmentNotFoundException(String message, String shipmentId) {
        super(message);
        this.shipmentId = shipmentId;
        this.trackingNumber = null;
        this.orderId = null;
    }
    
    /**
     * Constructor for shipment not found by tracking number
     * @param message the error message
     * @param trackingNumber the tracking number that was not found
     * @param isTrackingNumber flag to indicate this is a tracking number (not used, just for method signature distinction)
     */
    public ShipmentNotFoundException(String message, String trackingNumber, boolean isTrackingNumber) {
        super(message);
        this.shipmentId = null;
        this.trackingNumber = trackingNumber;
        this.orderId = null;
    }
    
    /**
     * Constructor for shipment not found by order ID
     * @param message the error message
     * @param orderId the order ID that was not found
     */
    public ShipmentNotFoundException(String message, Long orderId) {
        super(message);
        this.shipmentId = null;
        this.trackingNumber = null;
        this.orderId = orderId;
    }
    
    /**
     * Static factory method for shipment not found by ID
     * @param shipmentId the shipment ID
     * @return ShipmentNotFoundException instance
     */
    public static ShipmentNotFoundException byId(Long shipmentId) {
        return new ShipmentNotFoundException(
            "Shipment not found with ID: " + shipmentId,
            String.valueOf(shipmentId)
        );
    }
    
    /**
     * Static factory method for shipment not found by tracking number
     * @param trackingNumber the tracking number
     * @return ShipmentNotFoundException instance
     */
    public static ShipmentNotFoundException byTrackingNumber(String trackingNumber) {
        return new ShipmentNotFoundException(
            "Shipment not found with tracking number: " + trackingNumber,
            trackingNumber,
            true
        );
    }
    
    /**
     * Static factory method for shipment not found by order ID
     * @param orderId the order ID
     * @return ShipmentNotFoundException instance
     */
    public static ShipmentNotFoundException byOrderId(Long orderId) {
        return new ShipmentNotFoundException(
            "Shipment not found for order ID: " + orderId,
            orderId
        );
    }
    
    // Getters
    public String getShipmentId() {
        return shipmentId;
    }
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    /**
     * Get the identifier that was not found
     * @return the identifier (shipment ID, tracking number, or order ID)
     */
    public String getIdentifier() {
        if (shipmentId != null) {
            return shipmentId;
        } else if (trackingNumber != null) {
            return trackingNumber;
        } else if (orderId != null) {
            return String.valueOf(orderId);
        }
        return "unknown";
    }
    
    /**
     * Get the type of identifier that was not found
     * @return the identifier type
     */
    public String getIdentifierType() {
        if (shipmentId != null) {
            return "shipment ID";
        } else if (trackingNumber != null) {
            return "tracking number";
        } else if (orderId != null) {
            return "order ID";
        }
        return "unknown";
    }
}