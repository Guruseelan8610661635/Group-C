# Member 4: Payment & Pricing System 💳

## Your Responsibilities

You are responsible for the **Payment and Pricing** system of ParkEase.

### Backend Components
- **Controllers**: `PaymentController.java`, `AdminPricingController.java`, `PromotionController.java`
- **Models**: `Payment.java`, `Pricing.java`, `Promotion.java`
- **Repositories**: `PaymentRepository.java`
- **Services**: `PaymentService.java`, `PricingService.java`

### Frontend Components
- **Pages**: `Payment.jsx`
- **Components**: `PaymentModal.jsx`
- **Services**: `paymentService.js`, `pricingService.js`, `promotionService.js`

## Key Features to Implement
- ✅ Payment processing
- ✅ Pricing calculations
- ✅ Promotion/discount logic
- ✅ Payment history
- ✅ Auto-opening payment modal
- ✅ Payment status tracking

## Getting Started

### 1. Set up your feature branch
```bash
git checkout develop
git pull origin develop
git checkout -b feature/payment-gateway-integration
```

### 2. Review the files in this folder
- Study payment flow
- Understand pricing calculation
- Review promotion application logic

### 3. Make your changes
- Integrate payment gateway
- Implement discount logic
- Test payment scenarios

### 4. Submit Pull Request
- Push to your feature branch
- Create PR to `develop` branch
- Request review from team members

## Integration Points
You'll need to coordinate with:
- **Member 2**: Receive booking data for payment
- **Member 5**: Admin pricing and promotion management
- **Member 6**: Payment data for revenue reports

## Testing Checklist
- [ ] Payment modal opens automatically after checkout
- [ ] Pricing is calculated correctly
- [ ] Promotions are applied properly
- [ ] Payment confirmation updates booking status
- [ ] Payment history displays correctly
- [ ] Failed payments are handled gracefully

## Contact
For questions about payment processing, reach out in the team chat!
