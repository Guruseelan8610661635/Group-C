import { useEffect, useState } from 'react';
import { paymentService } from '../services/paymentService';
import { pricingService } from '../services/pricingService';

export default function PaymentModal({ booking, onClose, onPaymentSuccess }) {
  const [paymentMethod, setPaymentMethod] = useState('CARD');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [transactionId, setTransactionId] = useState(null);
  const [isProcessing, setIsProcessing] = useState(false);

  const [ratePerHour, setRatePerHour] = useState(null);
  const [liveMinutes, setLiveMinutes] = useState(booking.durationMinutes || 0);
  const [liveAmount, setLiveAmount] = useState(booking.parkingFee || 0);

  // Use liveAmount as amount to pay (updates every 30s)
  const amount = liveAmount || booking.parkingFee || 0;

  // Extract booking ID - handle both 'id' and 'bookingId' field names
  const bookingId = booking?.id || booking?.bookingId;

  const handlePayment = async () => {
    if (isProcessing) return;

    // Validate booking ID before proceeding
    if (!bookingId) {
      console.error('❌ Invalid booking object:', booking);
      setError('Booking ID is required. Please try again or contact support.');
      return;
    }

    setIsProcessing(true);
    setLoading(true);
    setError('');

    try {
      console.log('💳 Processing payment for booking:', bookingId, 'Amount:', amount, 'Method:', paymentMethod);
      console.log('📦 Full booking object:', JSON.stringify(booking, null, 2));

      const response = await paymentService.processPayment(
        bookingId,
        amount,
        paymentMethod
      );

      console.log('✅ Payment response:', response);

      if (response.success) {
        setTransactionId(response.transactionId);
        // Call success callback
        onPaymentSuccess(response);
      } else {
        setError(response.message || 'Payment failed. Please try again.');
      }
    } catch (err) {
      console.error('❌ Payment error:', err);
      console.error('❌ Error response:', err.response);
      const errorMsg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Failed to process payment. Please try again.';
      setError(errorMsg);
    } finally {
      setLoading(false);
      setIsProcessing(false);
    }
  };

  // Success screen
  if (transactionId) {
    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-2xl p-8 w-full max-w-sm shadow-2xl text-center space-y-6">
          <div className="text-6xl">✅</div>
          <div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Payment Successful!</h2>
            <p className="text-gray-600">Your parking fee has been processed.</p>
          </div>

          <div className="bg-green-50 p-4 rounded-lg border border-green-200 space-y-2 text-left">
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">Transaction ID:</span>
              <span className="font-mono font-bold text-green-700">{transactionId}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">Amount Paid:</span>
              <span className="font-bold text-gray-900">₹{amount.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">Payment Method:</span>
              <span className="font-bold text-gray-900">
                {paymentMethod === 'CARD' ? '💳 Card' : '📱 UPI'}
              </span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">Slot Released:</span>
              <span className="font-bold text-green-700">✓ Yes</span>
            </div>
          </div>

          <button
            onClick={onClose}
            className="w-full py-3 bg-green-600 hover:bg-green-700 text-white rounded-lg font-bold transition"
          >
            Done
          </button>
        </div>
      </div>
    );
  }

  // Payment options screen

  // Fetch pricing and set up live tick for duration and amount
  useEffect(() => {
    let mounted = true;
    const loadRate = async () => {
      try {
        const data = await pricingService.getPricingByType(booking.vehicleType || 'CAR');
        if (mounted) {
          const rate = data?.hourlyRate ?? data?.ratePerHour ?? 0;
          setRatePerHour(rate);
          // Initialize liveMinutes and liveAmount
          if (!booking.exitTime) {
            const minutes = Math.max(0, Math.floor((Date.now() - new Date(booking.entryTime).getTime()) / 60000));
            setLiveMinutes(minutes);
            setLiveAmount((rate * minutes) / 60);
          } else {
            setLiveMinutes(booking.durationMinutes || 0);
            setLiveAmount(booking.parkingFee || 0);
          }
        }
      } catch (e) {
        if (mounted) {
          setRatePerHour(0);
        }
      }
    };

    loadRate();

    const interval = setInterval(() => {
      if (!booking.exitTime) {
        const minutes = Math.max(0, Math.floor((Date.now() - new Date(booking.entryTime).getTime()) / 60000));
        setLiveMinutes(minutes);
        setLiveAmount(((ratePerHour || 0) * minutes) / 60);
      }
    }, 30000);

    return () => {
      mounted = false;
      clearInterval(interval);
    };
  }, [booking.entryTime, booking.exitTime, booking.vehicleType, ratePerHour]);
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl p-6 w-full max-w-sm shadow-2xl space-y-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">💳 Complete Payment</h2>
          <p className="text-gray-600 text-sm">
            {booking.locationName} • Slot #{booking.slotNumber}
          </p>
        </div>

        {/* Amount Card */}
        <div className="bg-white p-6 rounded-xl border border-gray-100 shadow-sm">
          <p className="text-gray-600 text-sm mb-2">Amount Due</p>
          <p className="text-4xl font-bold text-gray-900">₹{amount.toFixed(2)}</p>
          <p className="text-gray-500 text-xs mt-2">
            ⏱️ Duration: {liveMinutes} minutes
          </p>
          {/* Debug info - remove in production */}
          <p className="text-gray-400 text-xs mt-2">
            Booking ID: {bookingId || 'MISSING'}
          </p>
        </div>

        {/* Payment Method Selection */}
        <div className="space-y-3">
          <p className="font-semibold text-gray-800">Select Payment Method</p>

          {/* Card Option */}
          <button
            onClick={() => setPaymentMethod('CARD')}
            className={`w-full p-4 rounded-xl border-2 transition flex items-center gap-3 ${paymentMethod === 'CARD'
              ? 'border-blue-600 bg-blue-50'
              : 'border-gray-200 bg-white hover:border-gray-300'
              }`}
          >
            <div className="text-3xl">💳</div>
            <div className="text-left flex-1">
              <p className="font-bold text-gray-900">Credit/Debit Card</p>
              <p className="text-xs text-gray-500">Visa, Mastercard, Amex</p>
            </div>
            {paymentMethod === 'CARD' && <span className="text-2xl">✓</span>}
          </button>

          {/* UPI Option */}
          <button
            onClick={() => setPaymentMethod('UPI')}
            className={`w-full p-4 rounded-xl border-2 transition flex items-center gap-3 ${paymentMethod === 'UPI'
              ? 'border-blue-600 bg-blue-50'
              : 'border-gray-200 bg-white hover:border-gray-300'
              }`}
          >
            <div className="text-3xl">📱</div>
            <div className="text-left flex-1">
              <p className="font-bold text-gray-900">UPI</p>
              <p className="text-xs text-gray-500">Google Pay, PhonePe, Paytm</p>
            </div>
            {paymentMethod === 'UPI' && <span className="text-2xl">✓</span>}
          </button>
        </div>

        {/* Error Message */}
        {error && (
          <div className="bg-red-50 border border-red-200 p-3 rounded-lg">
            <p className="text-red-700 text-sm font-semibold">❌ {error}</p>
          </div>
        )}

        {/* Action Buttons */}
        <div className="flex gap-3">
          <button
            onClick={onClose}
            disabled={loading}
            className="flex-1 py-3 bg-white border border-black text-black rounded-lg font-bold transition disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            onClick={handlePayment}
            disabled={loading || isProcessing}
            className="flex-1 py-3 bg-black md:bg-blue-600 hover:bg-black md:hover:bg-blue-700 text-white rounded-lg font-bold transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {loading ? (
              <>
                <span className="animate-spin">⏳</span> Processing...
              </>
            ) : (
              <>Pay ₹{amount.toFixed(2)}</>
            )}
          </button>
        </div>

        <p className="text-xs text-gray-500 text-center">
          🔒 Your payment is secure and encrypted
        </p>
      </div>
    </div>
  );
}
