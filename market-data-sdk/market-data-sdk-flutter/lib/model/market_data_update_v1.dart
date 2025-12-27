//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.18

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class MarketDataUpdateV1 {
  /// Returns a new [MarketDataUpdateV1] instance.
  MarketDataUpdateV1({
    this.instrumentKey,
    this.lastPrice,
    this.change,
    this.open,
    this.high,
    this.low,
    this.previousClose,
    this.volume,
    this.timestamp,
    this.pChange,
  });

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? instrumentKey;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  double? lastPrice;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  double? change;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  double? open;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  double? high;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  double? low;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  double? previousClose;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  int? volume;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? timestamp;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  double? pChange;

  @override
  bool operator ==(Object other) => identical(this, other) || other is MarketDataUpdateV1 &&
    other.instrumentKey == instrumentKey &&
    other.lastPrice == lastPrice &&
    other.change == change &&
    other.open == open &&
    other.high == high &&
    other.low == low &&
    other.previousClose == previousClose &&
    other.volume == volume &&
    other.timestamp == timestamp &&
    other.pChange == pChange;

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (instrumentKey == null ? 0 : instrumentKey!.hashCode) +
    (lastPrice == null ? 0 : lastPrice!.hashCode) +
    (change == null ? 0 : change!.hashCode) +
    (open == null ? 0 : open!.hashCode) +
    (high == null ? 0 : high!.hashCode) +
    (low == null ? 0 : low!.hashCode) +
    (previousClose == null ? 0 : previousClose!.hashCode) +
    (volume == null ? 0 : volume!.hashCode) +
    (timestamp == null ? 0 : timestamp!.hashCode) +
    (pChange == null ? 0 : pChange!.hashCode);

  @override
  String toString() => 'MarketDataUpdateV1[instrumentKey=$instrumentKey, lastPrice=$lastPrice, change=$change, open=$open, high=$high, low=$low, previousClose=$previousClose, volume=$volume, timestamp=$timestamp, pChange=$pChange]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (this.instrumentKey != null) {
      json[r'instrumentKey'] = this.instrumentKey;
    } else {
      json[r'instrumentKey'] = null;
    }
    if (this.lastPrice != null) {
      json[r'lastPrice'] = this.lastPrice;
    } else {
      json[r'lastPrice'] = null;
    }
    if (this.change != null) {
      json[r'change'] = this.change;
    } else {
      json[r'change'] = null;
    }
    if (this.open != null) {
      json[r'open'] = this.open;
    } else {
      json[r'open'] = null;
    }
    if (this.high != null) {
      json[r'high'] = this.high;
    } else {
      json[r'high'] = null;
    }
    if (this.low != null) {
      json[r'low'] = this.low;
    } else {
      json[r'low'] = null;
    }
    if (this.previousClose != null) {
      json[r'previousClose'] = this.previousClose;
    } else {
      json[r'previousClose'] = null;
    }
    if (this.volume != null) {
      json[r'volume'] = this.volume;
    } else {
      json[r'volume'] = null;
    }
    if (this.timestamp != null) {
      json[r'timestamp'] = this.timestamp;
    } else {
      json[r'timestamp'] = null;
    }
    if (this.pChange != null) {
      json[r'pChange'] = this.pChange;
    } else {
      json[r'pChange'] = null;
    }
    return json;
  }

  /// Returns a new [MarketDataUpdateV1] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static MarketDataUpdateV1? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        requiredKeys.forEach((key) {
          assert(json.containsKey(key), 'Required key "MarketDataUpdateV1[$key]" is missing from JSON.');
          assert(json[key] != null, 'Required key "MarketDataUpdateV1[$key]" has a null value in JSON.');
        });
        return true;
      }());

      return MarketDataUpdateV1(
        instrumentKey: mapValueOfType<String>(json, r'instrumentKey'),
        lastPrice: mapValueOfType<double>(json, r'lastPrice'),
        change: mapValueOfType<double>(json, r'change'),
        open: mapValueOfType<double>(json, r'open'),
        high: mapValueOfType<double>(json, r'high'),
        low: mapValueOfType<double>(json, r'low'),
        previousClose: mapValueOfType<double>(json, r'previousClose'),
        volume: mapValueOfType<int>(json, r'volume'),
        timestamp: mapValueOfType<String>(json, r'timestamp'),
        pChange: mapValueOfType<double>(json, r'pChange'),
      );
    }
    return null;
  }

  static List<MarketDataUpdateV1> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <MarketDataUpdateV1>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = MarketDataUpdateV1.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, MarketDataUpdateV1> mapFromJson(dynamic json) {
    final map = <String, MarketDataUpdateV1>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = MarketDataUpdateV1.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of MarketDataUpdateV1-objects as value to a dart map
  static Map<String, List<MarketDataUpdateV1>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<MarketDataUpdateV1>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = MarketDataUpdateV1.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
  };
}

