import React, { useState, useEffect } from 'react';
import { reportService } from '../../services/reportService';
import axios from '../../api/axios';
import {
  BarChart, Bar, LineChart, Line, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8', '#82ca9d'];

const Reports = () => {
  const [reportType, setReportType] = useState('daily');
  const [usageReport, setUsageReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [customDates, setCustomDates] = useState({
    startDate: '',
    endDate: ''
  });

  const [locations, setLocations] = useState([]);
  const [slots, setSlots] = useState([]);
  const [exportFilters, setExportFilters] = useState({
    startDate: '',
    endDate: '',
    locationId: '',
    slotId: '',
    userId: ''
  });

  // New Analytics States
  const [locationPerformance, setLocationPerformance] = useState([]);
  const [slotUtilization, setSlotUtilization] = useState([]);
  const [topSlots, setTopSlots] = useState([]);
  const [leastSlots, setLeastSlots] = useState([]);
  const [revenueAnalytics, setRevenueAnalytics] = useState(null);
  const [userBehavior, setUserBehavior] = useState([]);
  const [topUsers, setTopUsers] = useState([]);
  const [heatmapData, setHeatmapData] = useState([]);
  const [analyticsLoading, setAnalyticsLoading] = useState(false);

  // Real-time Occupancy States
  const [realtimeOccupancy, setRealtimeOccupancy] = useState([]);
  const [realtimeLoading, setRealtimeLoading] = useState(false);
  const [autoRefresh, setAutoRefresh] = useState(false);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [activeAnalyticsTab, setActiveAnalyticsTab] = useState('locations');

  useEffect(() => {
    fetchReport(reportType);
  }, [reportType]);

  useEffect(() => {
    // Fetch locations and slots for filtering
    const fetchLocationsAndSlots = async () => {
      try {
        const [locationsRes, slotsRes] = await Promise.all([
          axios.get('/map/locations'),
          axios.get('/admin/slots')
        ]);
        setLocations(Array.isArray(locationsRes.data) ? locationsRes.data : []);
        setSlots(Array.isArray(slotsRes.data) ? slotsRes.data : []);
      } catch (err) {
        console.error('Error fetching locations/slots:', err);
      }
    };
    fetchLocationsAndSlots();
  }, []);

  const fetchReport = async (type) => {
    setLoading(true);
    setError(null);

    try {
      let data;
      switch (type) {
        case 'daily':
          data = await reportService.getDailyUsageReport();
          break;
        case 'weekly':
          data = await reportService.getWeeklyUsageReport();
          break;
        case 'monthly':
          data = await reportService.getMonthlyUsageReport();
          break;
        case 'custom':
          if (!customDates.startDate || !customDates.endDate) {
            setError('Please select both start and end dates');
            setLoading(false);
            return;
          }
          data = await reportService.getCustomUsageReport({
            startDate: new Date(customDates.startDate).toISOString(),
            endDate: new Date(customDates.endDate).toISOString(),
            reportType: 'CUSTOM'
          });
          break;
        default:
          data = await reportService.getDailyUsageReport();
      }
      setUsageReport(data);
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to fetch report');
      console.error('Error fetching report:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCustomDateSubmit = (e) => {
    e.preventDefault();
    fetchReport('custom');
  };

  const handleDownloadCsv = async () => {
    try {
      setError(null);
      const params = {};
      if (exportFilters.startDate && exportFilters.endDate) {
        params.startDate = new Date(exportFilters.startDate).toISOString();
        params.endDate = new Date(exportFilters.endDate).toISOString();
      }
      if (exportFilters.slotId) params.slotId = Number(exportFilters.slotId);
      if (exportFilters.userId) params.userId = Number(exportFilters.userId);

      const blob = await reportService.exportCsv(params);
      const url = window.URL.createObjectURL(new Blob([blob], { type: 'text/csv' }));
      const a = document.createElement('a');
      const rangePart = params.startDate && params.endDate
        ? `${params.startDate.substring(0, 10)}_${params.endDate.substring(0, 10)}`
        : 'all';
      const slotPart = params.slotId ? `-slot_${params.slotId}` : '';
      const userPart = params.userId ? `-user_${params.userId}` : '';
      a.href = url;
      a.download = `bookings_${rangePart}${slotPart}${userPart}.csv`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError(err?.response?.data?.error || err.message || 'Failed to export CSV');
      console.error('CSV export error:', err);
    }
  };

  // Fetch Advanced Analytics Data
  const fetchAdvancedAnalytics = async () => {
    setAnalyticsLoading(true);
    try {
      const [locations, slots, topSl, leastSl, revenue, users, topUsr, heatmap] = await Promise.all([
        reportService.getLocationComparison(),
        reportService.getSlotUtilization(),
        reportService.getTopSlots(10),
        reportService.getLeastSlots(10),
        reportService.getRevenueAnalytics(),
        reportService.getUserBehaviorAnalytics(),
        reportService.getTopUsers(10),
        reportService.getOccupancyHeatmap()
      ]);

      setLocationPerformance(locations);
      setSlotUtilization(slots);
      setTopSlots(topSl);
      setLeastSlots(leastSl);
      setRevenueAnalytics(revenue);
      setUserBehavior(users);
      setTopUsers(topUsr);
      setHeatmapData(heatmap);
    } catch (err) {
      console.error('Error fetching analytics:', err);
      setError(err?.message || 'Failed to fetch analytics');
    } finally {
      setAnalyticsLoading(false);
    }
  };

  useEffect(() => {
    fetchAdvancedAnalytics();
  }, []);

  // Fetch Real-Time Occupancy Data
  const fetchRealtimeOccupancy = async () => {
    setRealtimeLoading(true);
    try {
      const data = await reportService.getRealtimeOccupancy();
      setRealtimeOccupancy(data);
      setLastUpdated(new Date());
    } catch (err) {
      console.error('Error fetching realtime occupancy:', err);
      setError(err?.message || 'Failed to fetch real-time occupancy');
    } finally {
      setRealtimeLoading(false);
    }
  };


  // Auto-refresh effect for real-time occupancy - TEMPORARILY DISABLED
  // TODO: Re-enable when backend endpoint is fixed
  /*
  useEffect(() => {
    fetchRealtimeOccupancy(); // Initial fetch

    let interval;
    if (autoRefresh) {
      interval = setInterval(() => {
        fetchRealtimeOccupancy();
      }, 10000); // Refresh every 10 seconds
    }

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [autoRefresh]);
  */

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(value || 0);
  };

  const formatCurrencyCompact = (value) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
      notation: 'compact'
    }).format(value || 0);
  };

  const formatDuration = (minutes) => {
    if (!minutes) return '0 min';
    const hours = Math.floor(minutes / 60);
    const mins = Math.round(minutes % 60);
    return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`;
  };

  // Prepare chart data
  const prepareHourlyData = () => {
    if (!usageReport?.peakHoursData) return [];

    return Object.entries(usageReport.peakHoursData)
      .map(([hour, count]) => ({
        hour: `${hour}:00`,
        bookings: count
      }))
      .sort((a, b) => parseInt(a.hour) - parseInt(b.hour));
  };

  const prepareDailyData = () => {
    if (!usageReport?.dailyBreakdown) return [];

    return Object.entries(usageReport.dailyBreakdown)
      .map(([dateKey, count]) => ({
        dateKey,
        dateLabel: new Date(dateKey).toLocaleDateString('en-IN', { month: 'short', day: 'numeric' }),
        bookings: count,
        revenue: usageReport.dailyRevenueBreakdown?.[dateKey] || 0
      }))
      .sort((a, b) => new Date(a.dateKey) - new Date(b.dateKey))
      .map(({ dateLabel, bookings, revenue }) => ({
        date: dateLabel,
        bookings,
        revenue,
      }));
  };

  const prepareStatusData = () => {
    if (!usageReport?.statusDistribution) return [];

    return Object.entries(usageReport.statusDistribution)
      .map(([status, count]) => ({
        name: status,
        value: count
      }));
  };

  const prepareVehicleTypeData = () => {
    if (!usageReport?.vehicleTypeBreakdown) return [];

    return Object.entries(usageReport.vehicleTypeBreakdown)
      .map(([type, count]) => ({
        name: type,
        value: count
      }));
  };

  const hourlyData = prepareHourlyData();
  const dailyData = prepareDailyData();
  const statusData = prepareStatusData();
  const vehicleTypeData = prepareVehicleTypeData();

  const cardBase = 'bg-white rounded-2xl shadow-sm border border-gray-100';
  const pillBase = 'px-4 py-2 rounded-full font-medium transition-colors text-sm sm:text-base flex items-center justify-center';
  const valueTextCls = 'text-[clamp(1.25rem,3vw,1.75rem)] sm:text-2xl font-bold text-gray-900 tabular-nums tracking-tight break-words leading-tight';

  const summaryCards = usageReport ? [
    {
      title: 'Total Bookings',
      value: usageReport.totalBookings,
      accent: 'text-slate-700 bg-slate-50',
      icon: (
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
      ),
    },
    {
      title: 'Total Revenue',
      value: formatCurrencyCompact(usageReport.totalRevenue),
      accent: 'text-green-600 bg-green-50',
      icon: (
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      ),
    },
    {
      title: 'Avg Duration',
      value: formatDuration(usageReport.averageDurationMinutes),
      accent: 'text-purple-600 bg-purple-50',
      icon: (
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      ),
    },
    {
      title: 'Occupancy Rate',
      value: `${usageReport.occupancyRate}%`,
      accent: 'text-orange-600 bg-orange-50',
      icon: (
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
        </svg>
      ),
    },
  ] : [];

  const periodOptions = [
    { key: 'daily', label: 'Daily' },
    { key: 'weekly', label: 'Weekly' },
    { key: 'monthly', label: 'Monthly' },
    { key: 'custom', label: 'Custom' },
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading report...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-5 md:px-6">
      <div className="max-w-6xl mx-auto space-y-5">
        {/* Header */}
        <div className={`${cardBase} p-5 md:p-6 flex flex-col gap-3 md:flex-row md:items-center md:justify-between`}>
          <div className="space-y-1">
            <p className="text-xs uppercase tracking-wide text-gray-500">Admin Console · Reports</p>
            <h1 className="text-2xl md:text-3xl font-bold text-gray-900">Usage insights</h1>
            <p className="text-gray-600">See bookings, revenue, and utilization at a glance.</p>
          </div>
          <div className="flex items-center gap-3 text-sm text-gray-600">
            <span className="px-3 py-1 rounded-full bg-slate-50 text-slate-700 font-medium">{reportType.toUpperCase()}</span>
            <span className="hidden sm:block text-gray-500">Auto-refreshes when period changes</span>
          </div>
        </div>

        {/* Report Type Selection */}
        <div className={`${cardBase} p-5 md:p-6 space-y-4`}>
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p className="text-xs uppercase tracking-wide text-gray-500">Report period</p>
              <h2 className="text-xl font-semibold text-gray-900">Choose the time window</h2>
            </div>
            <span className="text-sm text-gray-500">Switch periods to refresh numbers</span>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            {periodOptions.map((option) => (
              <button
                key={option.key}
                onClick={() => setReportType(option.key)}
                className={`${pillBase} ${reportType === option.key
                  ? 'bg-slate-700 text-white shadow-sm'
                  : 'bg-slate-100 text-gray-700 hover:bg-slate-200'
                  }`}
              >
                {option.label}
              </button>
            ))}
          </div>

          {/* Custom Date Range */}
          {reportType === 'custom' && (
            <form onSubmit={handleCustomDateSubmit} className="pt-2 flex flex-wrap gap-3">
              <div className="flex-1 min-w-[220px]">
                <label className="block text-sm font-medium text-gray-700 mb-1">Start Date</label>
                <input
                  type="datetime-local"
                  value={customDates.startDate}
                  onChange={(e) => setCustomDates({ ...customDates, startDate: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500"
                  required
                />
              </div>
              <div className="flex-1 min-w-[220px]">
                <label className="block text-sm font-medium text-gray-700 mb-1">End Date</label>
                <input
                  type="datetime-local"
                  value={customDates.endDate}
                  onChange={(e) => setCustomDates({ ...customDates, endDate: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500"
                  required
                />
              </div>
              <div className="flex items-end">
                <button
                  type="submit"
                  className="px-6 py-2 bg-slate-700 text-white rounded-lg hover:bg-slate-800 transition-colors"
                >
                  Generate
                </button>
              </div>
            </form>
          )}
        </div>

        {/* Export Filters & Download */}
        <div className={`${cardBase} p-5 md:p-6 space-y-4`}>
          <div className="flex items-center justify-between flex-wrap gap-3">
            <div>
              <p className="text-xs uppercase tracking-wide text-gray-500">Export</p>
              <h2 className="text-xl font-semibold text-gray-900">Download CSV</h2>
              <p className="text-gray-600 text-sm">Filter by date range, location, slot, or user</p>
            </div>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-5 gap-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Start Date</label>
              <input
                type="datetime-local"
                value={exportFilters.startDate}
                onChange={(e) => setExportFilters({ ...exportFilters, startDate: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">End Date</label>
              <input
                type="datetime-local"
                value={exportFilters.endDate}
                onChange={(e) => setExportFilters({ ...exportFilters, endDate: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">📍 Location (optional)</label>
              <select
                value={exportFilters.locationId}
                onChange={(e) => setExportFilters({ ...exportFilters, locationId: e.target.value, slotId: '' })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Locations</option>
                {locations.map((location) => (
                  <option key={location.id} value={location.id}>
                    {location.name}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">🅿️ Slot (optional)</label>
              <select
                value={exportFilters.slotId}
                onChange={(e) => setExportFilters({ ...exportFilters, slotId: e.target.value })}
                disabled={!exportFilters.locationId}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
              >
                <option value="">All Slots</option>
                {exportFilters.locationId &&
                  slots
                    .filter((s) => (s.location?.id || s.locationId) === parseInt(exportFilters.locationId))
                    .map((slot) => (
                      <option key={slot.id} value={slot.id}>
                        {slot.slotNumber}
                      </option>
                    ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">User ID (optional)</label>
              <input
                type="number"
                min="1"
                placeholder="e.g. 101"
                value={exportFilters.userId}
                onChange={(e) => setExportFilters({ ...exportFilters, userId: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>
          <div className="flex justify-end">
            <button
              type="button"
              onClick={handleDownloadCsv}
              className="px-6 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors"
            >
              Download CSV
            </button>
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg shadow-sm">
            {error}
          </div>
        )}

        {/* Report Content */}
        {usageReport && (
          <>
            {/* Summary Cards */}
            <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
              {summaryCards.map((card, index) => (
                <div key={index} className={`${cardBase} p-5 flex items-center justify-between gap-4 min-h-[96px]`}>
                  <div className="space-y-1 min-w-0 overflow-hidden">
                    <p className="text-sm text-gray-600">{card.title}</p>
                    <p className={valueTextCls}>{card.value}</p>
                  </div>
                  <div className={`h-12 w-12 rounded-xl flex items-center justify-center ${card.accent}`}>
                    {card.icon}
                  </div>
                </div>
              ))}
            </div>

            {/* Additional Metrics */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className={`${cardBase} p-4 space-y-3`}>
                <div className="flex items-center justify-between">
                  <h3 className="text-sm font-semibold text-gray-800">Booking Status</h3>
                  <span className="text-xs text-gray-500">Live</span>
                </div>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Completed</span>
                    <span className="font-semibold text-green-600">{usageReport.completedBookings}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Active</span>
                    <span className="font-semibold text-slate-700">{usageReport.activeBookings}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Cancelled</span>
                    <span className="font-semibold text-red-600">{usageReport.cancelledBookings}</span>
                  </div>
                </div>
              </div>

              <div className={`${cardBase} p-4 space-y-3`}>
                <div className="flex items-center justify-between">
                  <h3 className="text-sm font-semibold text-gray-800">Revenue Metrics</h3>
                  <span className="text-xs text-gray-500">INR</span>
                </div>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Average Fee</span>
                    <span className="font-semibold">{formatCurrency(usageReport.averageFee)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Max Fee</span>
                    <span className="font-semibold">{formatCurrency(usageReport.maxFee)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Min Fee</span>
                    <span className="font-semibold">{formatCurrency(usageReport.minFee)}</span>
                  </div>
                </div>
              </div>

              <div className={`${cardBase} p-4 space-y-3`}>
                <div className="flex items-center justify-between">
                  <h3 className="text-sm font-semibold text-gray-800">Duration Stats</h3>
                  <span className="text-xs text-gray-500">Minutes</span>
                </div>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Median</span>
                    <span className="font-semibold">{formatDuration(usageReport.medianDurationMinutes)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Max</span>
                    <span className="font-semibold">{formatDuration(usageReport.maxDurationMinutes)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Min</span>
                    <span className="font-semibold">{formatDuration(usageReport.minDurationMinutes)}</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Peak Hours Chart */}
            <div className={`${cardBase} p-5 md:p-6 space-y-5`}>
              <div className="flex items-center justify-between">
                <div>
                  <h2 className="text-lg font-semibold text-gray-900">Top peak hours</h2>
                  <p className="text-sm text-gray-500">Based on entry times</p>
                </div>
                <span className="text-xs text-gray-500">Live occupancy signal</span>
              </div>
              {usageReport.topPeakHours && usageReport.topPeakHours.length > 0 ? (
                <>
                  <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
                    {usageReport.topPeakHours.map((peak, index) => (
                      <div key={index} className="bg-gradient-to-r from-slate-50 to-slate-100 rounded-xl p-4 text-center border border-slate-100">
                        <p className="text-xs text-gray-500">#{index + 1}</p>
                        <p className="text-lg font-semibold text-slate-700">{peak.timeRange}</p>
                        <p className="text-sm text-gray-700">{peak.bookingCount} bookings</p>
                      </div>
                    ))}
                  </div>

                  <div className="h-72">
                    <ResponsiveContainer width="100%" height={288}>
                      <BarChart data={hourlyData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="hour" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        <Bar dataKey="bookings" fill="#3b82f6" name="Bookings per Hour" />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </>
              ) : (
                <div className="text-center text-gray-500 py-6">No peak-hour data for this range.</div>
              )}
            </div>

            {/* Daily Breakdown Chart */}
            <div className={`${cardBase} p-5 md:p-6 space-y-4`}>
              <div className="flex items-center justify-between">
                <div>
                  <h2 className="text-lg font-semibold text-gray-900">Daily breakdown</h2>
                  <p className="text-sm text-gray-500">Bookings and revenue trend</p>
                </div>
                <span className="text-xs text-gray-500">Local time</span>
              </div>
              {dailyData.length > 0 ? (
                <div className="h-72">
                  <ResponsiveContainer width="100%" height={288}>
                    <LineChart data={dailyData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="date" />
                      <YAxis yAxisId="left" />
                      <YAxis yAxisId="right" orientation="right" />
                      <Tooltip />
                      <Legend />
                      <Line yAxisId="left" type="monotone" dataKey="bookings" stroke="#3b82f6" name="Bookings" />
                      <Line yAxisId="right" type="monotone" dataKey="revenue" stroke="#10b981" name="Revenue (₹)" />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <div className="text-center text-gray-500 py-6">No daily data for this range.</div>
              )}
            </div>

            {/* Pie Charts Row */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {statusData.length > 0 && (
                <div className={`${cardBase} p-6`}>
                  <h2 className="text-lg font-semibold mb-4 text-gray-900">Booking status distribution</h2>
                  <div className="h-72">
                    <ResponsiveContainer width="100%" height={288}>
                      <PieChart>
                        <Pie
                          data={statusData}
                          cx="50%"
                          cy="50%"
                          labelLine={false}
                          label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                          outerRadius={90}
                          fill="#8884d8"
                          dataKey="value"
                        >
                          {statusData.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                          ))}
                        </Pie>
                        <Tooltip />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              )}

              {vehicleTypeData.length > 0 && (
                <div className={`${cardBase} p-6`}>
                  <h2 className="text-lg font-semibold mb-4 text-gray-900">Vehicle type distribution</h2>
                  <div className="h-72">
                    <ResponsiveContainer width="100%" height={288}>
                      <PieChart>
                        <Pie
                          data={vehicleTypeData}
                          cx="50%"
                          cy="50%"
                          labelLine={false}
                          label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                          outerRadius={90}
                          fill="#82ca9d"
                          dataKey="value"
                        >
                          {vehicleTypeData.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                          ))}
                        </Pie>
                        <Tooltip />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              )}
            </div>

            {/* Report Info */}
            <div className={`${cardBase} p-4 text-sm text-gray-700 flex flex-wrap gap-3 items-center`}>
              <div className="flex items-center gap-2">
                <span className="text-xs uppercase tracking-wide text-gray-500">Report</span>
                <span className="font-semibold">{usageReport.reportType}</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-xs uppercase tracking-wide text-gray-500">Generated</span>
                <span>{new Date(usageReport.generatedDate).toLocaleString('en-IN')}</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-xs uppercase tracking-wide text-gray-500">Period</span>
                <span>{new Date(usageReport.startDate).toLocaleDateString('en-IN')} — {new Date(usageReport.endDate).toLocaleDateString('en-IN')}</span>
              </div>
            </div>
          </>
        )}

        {/* Advanced Analytics - Now inside max-w-6xl container */}
        {analyticsLoading ? (
          <div className={`${cardBase} p-12`}>
            <div className="flex flex-col items-center justify-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"></div>
              <p className="text-gray-600">Loading analytics...</p>
            </div>
          </div>
        ) : (
          <>
            {/* Location Performance - Matching Daily breakdown style */}
            {locationPerformance.length > 0 && (
              <div className={`${cardBase} p-5 md:p-6 space-y-4`}>
                <div className="flex items-center justify-between">
                  <div>
                    <h2 className="text-lg font-semibold text-gray-900">📍 Location Performance Comparison</h2>
                    <p className="text-sm text-gray-500">Revenue, bookings, and occupancy by location</p>
                  </div>
                  <button
                    onClick={fetchAdvancedAnalytics}
                    className="px-3 py-1.5 text-xs bg-slate-700 text-white rounded hover:bg-slate-800 transition-colors"
                  >
                    🔄 Refresh
                  </button>
                </div>
                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                  {locationPerformance.map((loc) => (
                    <div key={loc.locationId} className="border border-gray-200 rounded-xl p-4">
                      <h4 className="font-bold text-base text-gray-900 mb-3">{loc.locationName}</h4>
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-gray-600">Revenue:</span>
                          <span className="font-semibold text-green-600">{formatCurrency(loc.totalRevenue)}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-600">Bookings:</span>
                          <span className="font-semibold text-slate-700">{loc.totalBookings}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-600">Occupancy:</span>
                          <span className="font-semibold">{loc.occupancyRate?.toFixed(1)}%</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-600">Avg Duration:</span>
                          <span className="font-semibold">{formatDuration(loc.avgDuration)}</span>
                        </div>
                        <div className="flex justify-between pt-2 border-t border-gray-300">
                          <span className="text-gray-600">Slots:</span>
                          <span className="font-semibold">{loc.availableSlots} available / {loc.totalSlots} total</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Slot Utilization Analysis */}
            <div className="grid gap-4 lg:grid-cols-2">
              {/* Top Performers */}
              {topSlots.length > 0 && (
                <div className={`${cardBase} p-5 md:p-6 space-y-4`}>
                  <div>
                    <h2 className="text-lg font-semibold text-gray-900">🏆 Top Utilized Slots</h2>
                    <p className="text-sm text-gray-500">Best performing slots by utilization rate</p>
                  </div>
                  <div className="space-y-2">
                    {topSlots.slice(0, 5).map((slot, idx) => (
                      <div key={slot.slotId} className="flex items-center justify-between bg-green-50 border border-green-200 px-4 py-3 rounded-lg">
                        <div className="flex items-center gap-3">
                          <span className="font-bold text-lg text-gray-500">#{idx + 1}</span>
                          <div>
                            <p className="font-semibold text-gray-900">{slot.slotNumber}</p>
                            <p className="text-xs text-gray-500">{slot.locationName}</p>
                          </div>
                        </div>
                        <div className="text-right">
                          <p className="font-bold text-green-600">{slot.utilizationPercentage?.toFixed(1)}%</p>
                          <p className="text-xs text-gray-500">{slot.totalBookings} bookings</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Least Utilized */}
              {leastSlots.length > 0 && (
                <div className={`${cardBase} p-5 md:p-6 space-y-4`}>
                  <div>
                    <h2 className="text-lg font-semibold text-gray-900">⚠️ Underutilized Slots</h2>
                    <p className="text-sm text-gray-500">Slots requiring attention or optimization</p>
                  </div>
                  <div className="space-y-2">
                    {leastSlots.slice(0, 5).map((slot) => (
                      <div key={slot.slotId} className="flex items-center justify-between bg-orange-50 border border-orange-200 px-4 py-3 rounded-lg">
                        <div>
                          <p className="font-semibold text-gray-900">{slot.slotNumber}</p>
                          <p className="text-xs text-gray-500">{slot.locationName}</p>
                        </div>
                        <div className="text-right">
                          <p className="font-bold text-orange-600">{slot.utilizationPercentage?.toFixed(1)}%</p>
                          <p className="text-xs text-gray-500">Idle: {slot.idleTimeHours?.toFixed(0)}h</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* Revenue Analytics */}
            {revenueAnalytics && (
              <div className={`${cardBase} p-5 md:p-6 space-y-4`}>
                <div>
                  <h2 className="text-lg font-semibold text-gray-900">💰 Revenue Analytics</h2>
                  <p className="text-sm text-gray-500">Financial performance and distribution insights</p>
                </div>

                {/* Summary Cards */}
                <div className="grid gap-4 md:grid-cols-4">
                  <div className="bg-gradient-to-br from-green-500 to-emerald-600 text-white rounded-xl p-4">
                    <p className="text-xs opacity-90 mb-1">Total Revenue</p>
                    <p className="text-2xl font-bold">{formatCurrencyCompact(revenueAnalytics.totalRevenue)}</p>
                    <p className="text-xs opacity-75 mt-1">
                      {revenueAnalytics.revenueGrowth >= 0 ? '↗' : '↘'} {Math.abs(revenueAnalytics.revenueGrowth)?.toFixed(1)}% growth
                    </p>
                  </div>
                  <div className="bg-gray-50 border border-gray-200 rounded-xl p-4">
                    <p className="text-xs text-gray-600 mb-1">Daily Average</p>
                    <p className="text-xl font-bold text-gray-900">{formatCurrencyCompact(revenueAnalytics.dailyAvgRevenue)}</p>
                  </div>
                  <div className="bg-gray-50 border border-gray-200 rounded-xl p-4">
                    <p className="text-xs text-gray-600 mb-1">Weekly Average</p>
                    <p className="text-xl font-bold text-gray-900">{formatCurrencyCompact(revenueAnalytics.weeklyAvgRevenue)}</p>
                  </div>
                  <div className="bg-gray-50 border border-gray-200 rounded-xl p-4">
                    <p className="text-xs text-gray-600 mb-1">Peak Hour</p>
                    <p className="text-xl font-bold text-slate-700">{revenueAnalytics.peakHour}:00</p>
                    <p className="text-xs text-gray-500">{formatCurrencyCompact(revenueAnalytics.peakHourRevenue)}</p>
                  </div>
                </div>

                {/* Revenue by Location */}
                {revenueAnalytics.revenueByLocation && Object.keys(revenueAnalytics.revenueByLocation).length > 0 && (
                  <div>
                    <h4 className="font-semibold text-sm text-gray-700 mb-3">Revenue Distribution by Location</h4>
                    <div className="grid gap-2 md:grid-cols-2">
                      {Object.entries(revenueAnalytics.revenueByLocation).map(([location, revenue]) => (
                        <div key={location} className="flex justify-between items-center bg-gray-50 px-4 py-2 rounded-lg border border-gray-200">
                          <span className="text-sm font-medium text-gray-900">{location}</span>
                          <span className="text-sm font-bold text-green-600">{formatCurrency(revenue)}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* User Behavior */}
            {topUsers.length > 0 && (
              <div className={`${cardBase} p-5 md:p-6 space-y-4`}>
                <div>
                  <h2 className="text-lg font-semibold text-gray-900">👥 Top Users by Spending</h2>
                  <p className="text-sm text-gray-500">User segmentation and loyalty tracking</p>
                </div>
                <div className="space-y-2">
                  {topUsers.slice(0, 10).map((user, idx) => (
                    <div key={user.userId} className="flex items-center justify-between bg-purple-50 border border-purple-200 px-4 py-3 rounded-lg">
                      <div className="flex items-center gap-3">
                        <span className="font-bold text-lg text-gray-500">#{idx + 1}</span>
                        <div>
                          <p className="font-semibold text-gray-900 text-sm">{user.userName}</p>
                          <p className="text-xs text-gray-500">{user.email}</p>
                        </div>
                        <span className={`px-2 py-0.5 text-xs font-semibold rounded-full ${user.userSegment === 'VIP' ? 'bg-yellow-100 text-yellow-800' :
                          user.userSegment === 'REGULAR' ? 'bg-slate-100 text-slate-800' :
                            'bg-gray-200 text-gray-700'
                          }`}>
                          {user.userSegment}
                        </span>
                      </div>
                      <div className="text-right">
                        <p className="font-bold text-green-600 text-sm">{formatCurrency(user.totalSpent)}</p>
                        <p className="text-xs text-gray-500">{user.totalBookings} bookings</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Real-Time Occupancy Heatmap */}
            {realtimeOccupancy.length > 0 && (
              <div className={`${cardBase} p-5 md:p-6 space-y-4`}>
                <div className="flex justify-between items-center">
                  <div>
                    <h2 className="text-lg font-semibold text-gray-900">🔥 Real-Time Occupancy</h2>
                    <p className="text-sm text-gray-500">Live status of all parking slots</p>
                  </div>
                  <div className="flex items-center gap-3">
                    {lastUpdated && (
                      <span className="text-xs text-gray-500">
                        Updated: {lastUpdated.toLocaleTimeString()}
                      </span>
                    )}
                    <button
                      onClick={() => setAutoRefresh(!autoRefresh)}
                      className={`px-3 py-1.5 text-xs rounded-lg font-medium transition-colors ${autoRefresh
                        ? 'bg-green-600 text-white'
                        : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                        }`}
                    >
                      {autoRefresh ? '🔄 Auto-Refresh ON' : '⏸️ Auto-Refresh OFF'}
                    </button>
                    <button
                      onClick={fetchRealtimeOccupancy}
                      disabled={realtimeLoading}
                      className="px-3 py-1.5 text-xs bg-slate-700 text-white rounded-lg hover:bg-slate-800 transition-colors disabled:opacity-50"
                    >
                      {realtimeLoading ? '⏳ Loading...' : '🔄 Refresh Now'}
                    </button>
                  </div>
                </div>

                {/* Legend */}
                <div className="flex items-center gap-4 text-xs flex-wrap">
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 bg-green-500 rounded"></div>
                    <span>Available</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 bg-red-500 rounded"></div>
                    <span>Occupied</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 bg-yellow-500 rounded"></div>
                    <span>Reserved</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 bg-gray-400 rounded"></div>
                    <span>Maintenance</span>
                  </div>
                </div>

                {/* Location-wise Occupancy Grid */}
                <div className="space-y-6">
                  {realtimeOccupancy.map((location) => (
                    <div key={location.locationId} className="border border-gray-200 rounded-xl p-4">
                      <div className="flex justify-between items-center mb-4">
                        <h3 className="font-bold text-base text-gray-900">📍 {location.locationName}</h3>
                        <div className="text-sm">
                          <span className="font-semibold text-gray-900">{location.occupancyPercentage?.toFixed(1)}%</span>
                          <span className="text-gray-500"> Occupied</span>
                        </div>
                      </div>

                      {/* Summary Stats */}
                      <div className="grid grid-cols-4 gap-2 mb-4">
                        <div className="bg-green-50 border border-green-200 rounded-lg p-2 text-center">
                          <div className="text-lg font-bold text-green-600">{location.availableSlots}</div>
                          <div className="text-xs text-gray-600">Available</div>
                        </div>
                        <div className="bg-red-50 border border-red-200 rounded-lg p-2 text-center">
                          <div className="text-lg font-bold text-red-600">{location.occupiedSlots}</div>
                          <div className="text-xs text-gray-600">Occupied</div>
                        </div>
                        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-2 text-center">
                          <div className="text-lg font-bold text-yellow-600">{location.reservedSlots}</div>
                          <div className="text-xs text-gray-600">Reserved</div>
                        </div>
                        <div className="bg-gray-100 border border-gray-300 rounded-lg p-2 text-center">
                          <div className="text-lg font-bold text-gray-600">{location.maintenanceSlots}</div>
                          <div className="text-xs text-gray-600">Maintenance</div>
                        </div>
                      </div>

                      {/* Slot Grid */}
                      <div className="grid grid-cols-6 sm:grid-cols-8 md:grid-cols-10 lg:grid-cols-12 gap-2">
                        {location.slots?.map((slot) => {
                          const bgColor =
                            slot.status === 'OCCUPIED' ? 'bg-red-500' :
                              slot.status === 'RESERVED' ? 'bg-yellow-500' :
                                slot.status === 'UNDER_MAINTENANCE' ? 'bg-gray-400' :
                                  'bg-green-500';

                          return (
                            <div
                              key={slot.slotId}
                              className={`${bgColor} text-white rounded-lg p-2 text-center cursor-pointer hover:opacity-80 transition-opacity`}
                              title={`${slot.slotNumber} - ${slot.status}`}
                            >
                              <div className="text-xs font-semibold">{slot.slotNumber}</div>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default Reports;
