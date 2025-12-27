//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.18

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class NSEStockIndicesDataV1 {
  /// Returns a new [NSEStockIndicesDataV1] instance.
  NSEStockIndicesDataV1({
    this.name,
    this.advance,
    this.timestamp,
    this.data = const [],
    this.metadata,
    this.marketStatus,
    this.date30dAgo,
    this.date365dAgo,
  });

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? name;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  Advance? advance;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? timestamp;

  List<StockData> data;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  IndexMetadata? metadata;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  MarketStatus? marketStatus;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? date30dAgo;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? date365dAgo;

  @override
  bool operator ==(Object other) => identical(this, other) || other is NSEStockIndicesDataV1 &&
    other.name == name &&
    other.advance == advance &&
    other.timestamp == timestamp &&
    _deepEquality.equals(other.data, data) &&
    other.metadata == metadata &&
    other.marketStatus == marketStatus &&
    other.date30dAgo == date30dAgo &&
    other.date365dAgo == date365dAgo;

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (name == null ? 0 : name!.hashCode) +
    (advance == null ? 0 : advance!.hashCode) +
    (timestamp == null ? 0 : timestamp!.hashCode) +
    (data.hashCode) +
    (metadata == null ? 0 : metadata!.hashCode) +
    (marketStatus == null ? 0 : marketStatus!.hashCode) +
    (date30dAgo == null ? 0 : date30dAgo!.hashCode) +
    (date365dAgo == null ? 0 : date365dAgo!.hashCode);

  @override
  String toString() => 'NSEStockIndicesDataV1[name=$name, advance=$advance, timestamp=$timestamp, data=$data, metadata=$metadata, marketStatus=$marketStatus, date30dAgo=$date30dAgo, date365dAgo=$date365dAgo]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (this.name != null) {
      json[r'name'] = this.name;
    } else {
      json[r'name'] = null;
    }
    if (this.advance != null) {
      json[r'advance'] = this.advance;
    } else {
      json[r'advance'] = null;
    }
    if (this.timestamp != null) {
      json[r'timestamp'] = this.timestamp;
    } else {
      json[r'timestamp'] = null;
    }
      json[r'data'] = this.data;
    if (this.metadata != null) {
      json[r'metadata'] = this.metadata;
    } else {
      json[r'metadata'] = null;
    }
    if (this.marketStatus != null) {
      json[r'marketStatus'] = this.marketStatus;
    } else {
      json[r'marketStatus'] = null;
    }
    if (this.date30dAgo != null) {
      json[r'date30dAgo'] = this.date30dAgo;
    } else {
      json[r'date30dAgo'] = null;
    }
    if (this.date365dAgo != null) {
      json[r'date365dAgo'] = this.date365dAgo;
    } else {
      json[r'date365dAgo'] = null;
    }
    return json;
  }

  /// Returns a new [NSEStockIndicesDataV1] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static NSEStockIndicesDataV1? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        requiredKeys.forEach((key) {
          assert(json.containsKey(key), 'Required key "NSEStockIndicesDataV1[$key]" is missing from JSON.');
          assert(json[key] != null, 'Required key "NSEStockIndicesDataV1[$key]" has a null value in JSON.');
        });
        return true;
      }());

      return NSEStockIndicesDataV1(
        name: mapValueOfType<String>(json, r'name'),
        advance: Advance.fromJson(json[r'advance']),
        timestamp: mapValueOfType<String>(json, r'timestamp'),
        data: StockData.listFromJson(json[r'data']),
        metadata: IndexMetadata.fromJson(json[r'metadata']),
        marketStatus: MarketStatus.fromJson(json[r'marketStatus']),
        date30dAgo: mapValueOfType<String>(json, r'date30dAgo'),
        date365dAgo: mapValueOfType<String>(json, r'date365dAgo'),
      );
    }
    return null;
  }

  static List<NSEStockIndicesDataV1> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <NSEStockIndicesDataV1>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = NSEStockIndicesDataV1.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, NSEStockIndicesDataV1> mapFromJson(dynamic json) {
    final map = <String, NSEStockIndicesDataV1>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = NSEStockIndicesDataV1.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of NSEStockIndicesDataV1-objects as value to a dart map
  static Map<String, List<NSEStockIndicesDataV1>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<NSEStockIndicesDataV1>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = NSEStockIndicesDataV1.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
  };
}

