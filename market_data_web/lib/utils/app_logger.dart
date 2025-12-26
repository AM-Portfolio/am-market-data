
import 'package:flutter/foundation.dart';
import 'package:intl/intl.dart';

enum LogLevel {
  debug,
  info,
  warning,
  error,
}

class AppLogger {
  static final AppLogger _instance = AppLogger._internal();

  factory AppLogger() {
    return _instance;
  }

  AppLogger._internal();

  /// Logs a message with a specific level, tag (File.method), and content.
  void _log({
    required LogLevel level,
    required String tag,
    required String message,
    Object? error,
    StackTrace? stackTrace,
  }) {
    final timestamp = DateFormat('HH:mm:ss.SSS').format(DateTime.now());
    final levelStr = level.toString().split('.').last.toUpperCase();
    final logMessage = '[$timestamp] [$levelStr] [$tag] $message';

    if (kDebugMode) {
      if (level == LogLevel.error) {
        print('\x1B[31m$logMessage\x1B[0m'); // Red for error
        if (error != null) print('\x1B[31mError: $error\x1B[0m');
        if (stackTrace != null) print('\x1B[31m$stackTrace\x1B[0m');
      } else if (level == LogLevel.warning) {
        print('\x1B[33m$logMessage\x1B[0m'); // Yellow for warning
      } else if (level == LogLevel.info) {
        print('\x1B[34m$logMessage\x1B[0m'); // Blue for info
      } else {
        print(logMessage); // Default
      }
    }
  }

  // Convenience methods
  static void debug(String tag, String message) {
    _instance._log(level: LogLevel.debug, tag: tag, message: message);
  }

  static void info(String tag, String message) {
    _instance._log(level: LogLevel.info, tag: tag, message: message);
  }

  static void warning(String tag, String message, [Object? error, StackTrace? stackTrace]) {
    _instance._log(level: LogLevel.warning, tag: tag, message: message, error: error, stackTrace: stackTrace);
  }

  static void error(String tag, String message, [Object? error, StackTrace? stackTrace]) {
    _instance._log(level: LogLevel.error, tag: tag, message: message, error: error, stackTrace: stackTrace);
  }
  
  // Static wrapper for generic log call
  static void log({
    required LogLevel level,
    required String tag,
    required String message,
    Object? error,
    StackTrace? stackTrace,
  }) {
    _instance._log(level: level, tag: tag, message: message, error: error, stackTrace: stackTrace);
  }
}

