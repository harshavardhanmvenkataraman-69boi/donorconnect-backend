package com.donorconnect.billingservice.dto;

/*
 * ─── Payment DTOs Reference ───────────────────────────────────────────────────
 *
 * This file serves as a reference/overview of all Payment DTO classes used in
 * the payment gateway integration. Each DTO is defined in its own separate file.
 *
 * DTO Files:
 *  - PaymentOrderRequestDTO.java   → Step 1: Frontend asks backend to create a Razorpay order
 *  - PaymentOrderResponseDTO.java  → Step 1: Backend responds with order details for Razorpay Checkout
 *  - PaymentVerifyRequestDTO.java  → Step 2: Frontend sends payment confirmation after Razorpay Checkout
 *  - PaymentVerifyResponseDTO.java → Step 2: Backend responds with final verification result
 *  - PaymentTransactionDTO.java    → General transaction info DTO
 *
 * Flow:
 *  1. Frontend sends PaymentOrderRequestDTO  → Backend creates Razorpay order
 *  2. Backend returns PaymentOrderResponseDTO (includes razorpayOrderId, amount, keyId)
 *  3. Frontend opens Razorpay Checkout, user pays
 *  4. Frontend sends PaymentVerifyRequestDTO (razorpayOrderId, paymentId, signature)
 *  5. Backend verifies signature and returns PaymentVerifyResponseDTO
 * ─────────────────────────────────────────────────────────────────────────────
 */
