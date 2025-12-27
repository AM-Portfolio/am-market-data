//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.18

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class MarketStatus {
  /// Returns a new [MarketStatus] instance.
  MarketStatus({
    this.market,
    this.marketStatus,
    this.tradeDate,
    this.index,
    this.last,
    this.variation,
    this.percentChange,
    this.marketStatusMessage,
  });

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? market;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? marketStatus;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? tradeDate;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? index;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  double? last;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  double? variation;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  double? percentChange;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? marketStatusMessage;

  @override
  bool operator ==(Object other) => identical(this, other) || other is MarketStatus &&
    other.market == market &&
    other.marketStatus == marketStatus &&
    other.tradeDate == tradeDate &&
    other.index == index &&
    other.last == last &&
    other.variation == variation &&
    other.percentChange == percentChange &&
    other.marketStatusMessage == marketStatusMessage;

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (market == null ? 0 : market!.hashCode) +
    (marketStatus == null ? 0 : marketStatus!.hashCode) +
    (tradeDate == null ? 0 : tradeDate!.hashCode) +
    (index == null ? 0 : index!.hashCode) +
    (last == null ? 0 : last!.hashCode) +
    (variation == null ? 0 : variation!.hashCode) +
    (percentChange == null ? 0 : percentChange!.hashCode) +
    (marketStatusMessage == null ? 0 : marketStatusMessage!.hashCode);

  @override
  String toString() => 'MarketStatus[market=$market, marketStatus=$marketStatus, tradeDate=$tradeDate, index=$index, last=$last, variation=$variation, percentChange=$percentChange, marketStatusMessage=$marketStatusMessage]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (this.market != null) {
      json[r'market'] = this.market;
    } else {
      json[r'market'] = null;
    }
    if (this.marketStatus != null) {
      json[r'marketStatus'] = this.marketStatus;
    } else {
      json[r'marketStatus'] = null;
    }
    if (this.tradeDate != null) {
      json[r'tradeDate'] = this.tradeDate;
    } else {
      json[r'tradeDate'] = null;
    }
    if (this.index != null) {
      json[r'index'] = this.index;
    } else {
      json[r'index'] = null;
    }
    if (this.last != null) {
      json[r'last'] = this.last;
    } else {
      json[r'last'] = null;
    }
    if (this.variation != null) {
      json[r'variation'] = this.variation;
    } else {
      json[r'variation'] = null;
    }
    if (this.percentChange != null) {
      json[r'percentChange'] = this.percentChange;
    } else {
      json[r'percentChange'] = null;
    }
    if (this.marketStatusMessage != null) {
      json[r'marketStatusMessage'] = this.marketStatusMessage;
    } else {
      json[r'marketStatusMessage'] = null;
    }
    return json;
  }

  /// Returns a new [MarketStatus] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static MarketStatus? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        requiredKeys.forEach((key) {
          assert(json.containsKey(key), 'Required key "MarketStatus[$key]" is missing from JSON.');
          assert(json[key] != null, 'Required key "MarketStatus[$key]" has a null value in JSON.');
        });
        return true;
      }());

      return MarketStatus(
        market: mapValueOfType<String>(json, r'market'),
        marketStatus: mapValueOfType<String>(json, r'marketStatus'),
        tradeDate: mapValueOfType<String>(json, r'tradeDate'),
        index: mapValueOfType<String>(json, r'index'),
        last: mapValueOfType<double>(json, r'last'),
        variation: mapValueOfType<double>(json, r'variation'),
        percentChange: mapValueOfType<double>(json, r'percentChange'),
        marketStatusMessage: mapValueOfType<String>(json, r'marketStatusMessage'),
      );
    }
    return null;
  }

  static List<MarketStatus> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <MarketStatus>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = MarketStatus.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, MarketStatus> mapFromJson(dynamic json) {
    final map = <String, MarketStatus>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = MarketStatus.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of MarketStatus-objects as value to a dart map
  static Map<String, List<MarketStatus>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<MarketStatus>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = MarketStatus.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
  };
}

