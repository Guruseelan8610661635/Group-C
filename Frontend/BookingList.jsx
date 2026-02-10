/**
 * BookingList Component
 * Displays current and past bookings with live timer
 */
import { useState, useEffect } from "react";

export default function BookingList({ currentBookings, pastBookings, loading, error }) {
  const [timers, setTimers] = useState({});

  // Update timers for active bookings every second
  useEffect(() => {
    const interval = setInterval(() => {
      const newTimers = {};
      currentBookings?.forEach((booking) => {
        if (booking.entryTime || booking.checkInTime) {
          // Parse the entry time - handle both ISO format and other formats
          const entryTimeStr = booking.entryTime || booking.checkInTime;
          let entryTime;

          // Try to parse as ISO date first (backend format)
          if (typeof entryTimeStr === 'string') {
            // If it's already an ISO string, use it directly
            entryTime = new Date(entryTimeStr);
          } else {
            entryTime = new Date(entryTimeStr);
          }

          // Validate the date
          if (isNaN(entryTime.getTime())) {
            console.error(`Invalid entry time for booking ${booking.id}:`, entryTimeStr);
            newTimers[booking.id] = "00:00:00";
            return;
          }

          const now = new Date();
          const diffMilliseconds = now - entryTime;

          // Prevent negative durations (entry time in the future)
          if (diffMilliseconds < 0) {
            console.warn(`Entry time is in the future for booking ${booking.id}:`, entryTimeStr);
            newTimers[booking.id] = "00:00:00";
            return;
          }

          const diffSeconds = Math.floor(diffMilliseconds / 1000);

          const hours = Math.floor(diffSeconds / 3600);
          const minutes = Math.floor((diffSeconds % 3600) / 60);
          const seconds = diffSeconds % 60;

          newTimers[booking.id] = `${String(hours).padStart(2, "0")}:${String(
            minutes
          ).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
        }
      });
      setTimers(newTimers);
    }, 1000);

    return () => clearInterval(interval);
  }, [currentBookings]);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
        <p className="text-gray-600 text-sm">Loading bookings...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-4">
        <p className="text-red-700 text-sm font-semibold">❌ Error loading bookings</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Current Bookings */}
      <div>
        <h3 className="font-bold text-gray-800 mb-3 text-lg flex items-center gap-2">
          <span>🟢 Active Bookings ({currentBookings?.length || 0})</span>
        </h3>

        {currentBookings && currentBookings.length > 0 ? (
          <>
            {/* Desktop cards (md+) */}
            <div className="hidden md:block space-y-3">
              {currentBookings.map((booking) => (
                <div
                  key={booking.id}
                  className="bg-green-50 border border-green-200 rounded-lg p-3"
                >
                  <div className="flex items-start justify-between mb-2">
                    <div>
                      <p className="font-semibold text-gray-800 text-base">
                        {booking.user?.name || booking.user?.username || "Unknown"}
                      </p>
                      <p className="text-sm text-gray-600">
                        🅿️ Slot {booking.slot?.slotNumber || booking.slotNumber}
                      </p>
                    </div>
                    <span className="bg-green-500 text-white text-[10px] font-bold px-2 py-1 rounded">
                      ACTIVE
                    </span>
                  </div>

                  <div className="grid grid-cols-2 gap-3 text-sm text-gray-700 mb-3">
                    <div>
                      <p className="text-gray-600">Vehicle:</p>
                      <p className="font-semibold">
                        {booking.vehicleNumber || booking.vehicle?.registrationNumber || "N/A"}
                      </p>
                    </div>
                    <div>
                      <p className="text-gray-600">Duration:</p>
                      <p className="font-semibold text-slate-700">
                        {timers[booking.id] || "00:00:00"}
                      </p>
                    </div>
                  </div>

                  <p className="text-[10px] text-gray-600">
                    ⏰ Started: {new Date(booking.entryTime || booking.checkInTime).toLocaleTimeString()}
                  </p>
                </div>
              ))}
            </div>

            {/* Mobile B/W cards */}
            <div className="md:hidden space-y-3">
              {currentBookings.map((booking) => (
                <div key={booking.id} className="bg-white rounded-xl p-4 border border-black/10 shadow-md">
                  <div className="flex items-start justify-between mb-2">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 bg-black rounded-full flex items-center justify-center text-white font-semibold">{(booking.user?.name || booking.user?.username || 'U').charAt(0).toUpperCase()}</div>
                      <div>
                        <p className="font-bold text-slate-900">{booking.user?.name || booking.user?.username || "Unknown"}</p>
                        <p className="text-xs text-gray-500">🅿️ Slot {booking.slot?.slotNumber || booking.slotNumber}</p>
                      </div>
                    </div>
                    <div className="text-right">
                      <span className="px-2 py-1 rounded text-xs font-semibold border border-black text-slate-900 bg-white">ACTIVE</span>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-2 text-xs text-gray-600 mb-2">
                    <div>
                      <p className="text-gray-500">Vehicle</p>
                      <p className="font-semibold text-slate-900">{booking.vehicleNumber || booking.vehicle?.registrationNumber || 'N/A'}</p>
                    </div>
                    <div className="text-right">
                      <p className="text-gray-500">Duration</p>
                      <p className="font-semibold text-slate-900">{timers[booking.id] || '00:00:00'}</p>
                    </div>
                  </div>

                  <p className="text-xs text-gray-500">⏰ {new Date(booking.entryTime || booking.checkInTime).toLocaleTimeString()}</p>
                </div>
              ))}
            </div>
          </>
        ) : (
          <div className="bg-gray-50 border border-gray-200 rounded-lg p-4 text-center">
            <p className="text-gray-600 text-sm">No active bookings</p>
          </div>
        )}
      </div>

      {/* Past Bookings */}
      <div>
        <h3 className="font-bold text-gray-800 mb-3 text-lg flex items-center gap-2">
          <span>🕒 Past Bookings ({pastBookings?.length || 0})</span>
        </h3>

        {pastBookings && pastBookings.length > 0 ? (
          <>
            <div className="hidden md:block space-y-2 max-h-[60vh] overflow-y-auto">
              {pastBookings.map((booking) => (
                <div
                  key={booking.id}
                  className="bg-gray-50 border border-gray-200 rounded-lg p-4"
                >
                  <div className="flex items-start justify-between mb-3">
                    <div>
                      <p className="font-semibold text-gray-800 text-base">
                        {booking.user?.name || booking.user?.username || "Unknown"}
                      </p>
                      <p className="text-sm text-gray-600">
                        🅿️ Slot {booking.slot?.slotNumber || booking.slotNumber}
                      </p>
                    </div>
                    <span className={`text-sm font-bold px-3 py-1.5 rounded ${booking.status === "COMPLETED"
                        ? "bg-slate-100 text-slate-700"
                        : "bg-gray-200 text-gray-700"
                      }`}>
                      {booking.status || "COMPLETED"}
                    </span>
                  </div>

                  <div className="grid grid-cols-2 gap-3 text-sm text-gray-700">
                    <div>
                      <p className="text-gray-600">Duration:</p>
                      <p className="font-semibold">{booking.duration || "N/A"}</p>
                    </div>
                    <div>
                      <p className="text-gray-600">Fee:</p>
                      <p className="font-semibold">₹{booking.totalCost || "0"}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div className="md:hidden space-y-3">
              {pastBookings.map((booking) => (
                <div key={booking.id} className="bg-white rounded-xl p-4 border border-black/10 shadow-md">
                  <div className="flex items-start justify-between mb-2">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 bg-black rounded-full flex items-center justify-center text-white font-semibold">{(booking.user?.name || booking.user?.username || 'U').charAt(0).toUpperCase()}</div>
                      <div>
                        <p className="font-bold text-slate-900">{booking.user?.name || booking.user?.username || "Unknown"}</p>
                        <p className="text-xs text-gray-500">🅿️ Slot {booking.slot?.slotNumber || booking.slotNumber}</p>
                      </div>
                    </div>

                    <div className="text-right">
                      <span className="px-2 py-1 rounded text-xs font-semibold border border-black text-slate-900 bg-white">{booking.status || 'COMPLETED'}</span>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-2 text-xs text-gray-600 mb-2">
                    <div>
                      <p className="text-gray-500">Duration</p>
                      <p className="font-semibold text-slate-900">{booking.duration || 'N/A'}</p>
                    </div>
                    <div className="text-right">
                      <p className="text-gray-500">Fee</p>
                      <p className="font-semibold text-slate-900">₹{booking.totalCost || '0'}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </>
        ) : (
          <div className="bg-gray-50 border border-gray-200 rounded-lg p-4 text-center">
            <p className="text-gray-600 text-sm">No past bookings</p>
          </div>
        )}
      </div>
    </div>
  );
}
