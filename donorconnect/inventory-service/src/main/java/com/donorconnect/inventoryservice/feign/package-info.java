/**
 * inventory-service has NO outgoing Feign clients.
 *
 * It is a self-contained read service — all data it needs
 * (bloodGroup, rhFactor, componentType, expiryDate) is denormalized
 * into InventoryBalance at creation time.
 *
 * blood-supply-service calls THIS service via its OWN Feign client:
 *   BloodSupplyToInventoryFeignClient → POST /api/v1/inventory/entry
 *   BloodSupplyToInventoryFeignClient → PATCH /api/v1/inventory/{componentId}/status
 */
package com.donorconnect.inventoryservice.feign;