//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.18

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;


class SecurityMetadataApi {
  SecurityMetadataApi([ApiClient? apiClient]) : apiClient = apiClient ?? defaultApiClient;

  final ApiClient apiClient;

  /// Find securities by symbols
  ///
  /// Returns metadata for specific symbols
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] symbols (required):
  Future<Response> findBySymbolsWithHttpInfo(String symbols,) async {
    // ignore: prefer_const_declarations
    final path = r'/api/v1/security/find';

    // ignore: prefer_final_locals
    Object? postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

      queryParams.addAll(_queryParams('', 'symbols', symbols));

    const contentTypes = <String>[];


    return apiClient.invokeAPI(
      path,
      'GET',
      queryParams,
      postBody,
      headerParams,
      formParams,
      contentTypes.isEmpty ? null : contentTypes.first,
    );
  }

  /// Find securities by symbols
  ///
  /// Returns metadata for specific symbols
  ///
  /// Parameters:
  ///
  /// * [String] symbols (required):
  Future<List<SecurityDTOV1>?> findBySymbols(String symbols,) async {
    final response = await findBySymbolsWithHttpInfo(symbols,);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      final responseBody = await _decodeBodyBytes(response);
      return (await apiClient.deserializeAsync(responseBody, 'List<SecurityDTOV1>') as List)
        .cast<SecurityDTOV1>()
        .toList(growable: false);

    }
    return null;
  }

  /// Get all securities
  ///
  /// Returns all available securities in the database
  ///
  /// Note: This method returns the HTTP [Response].
  Future<Response> getAllWithHttpInfo() async {
    // ignore: prefer_const_declarations
    final path = r'/api/v1/security/all';

    // ignore: prefer_final_locals
    Object? postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    const contentTypes = <String>[];


    return apiClient.invokeAPI(
      path,
      'GET',
      queryParams,
      postBody,
      headerParams,
      formParams,
      contentTypes.isEmpty ? null : contentTypes.first,
    );
  }

  /// Get all securities
  ///
  /// Returns all available securities in the database
  Future<List<SecurityDTOV1>?> getAll() async {
    final response = await getAllWithHttpInfo();
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      final responseBody = await _decodeBodyBytes(response);
      return (await apiClient.deserializeAsync(responseBody, 'List<SecurityDTOV1>') as List)
        .cast<SecurityDTOV1>()
        .toList(growable: false);

    }
    return null;
  }

  /// Get sectors for symbols
  ///
  /// Returns a mapping of symbol to sector
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] symbols (required):
  Future<Response> getSectorsWithHttpInfo(String symbols,) async {
    // ignore: prefer_const_declarations
    final path = r'/api/v1/security/sectors';

    // ignore: prefer_final_locals
    Object? postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

      queryParams.addAll(_queryParams('', 'symbols', symbols));

    const contentTypes = <String>[];


    return apiClient.invokeAPI(
      path,
      'GET',
      queryParams,
      postBody,
      headerParams,
      formParams,
      contentTypes.isEmpty ? null : contentTypes.first,
    );
  }

  /// Get sectors for symbols
  ///
  /// Returns a mapping of symbol to sector
  ///
  /// Parameters:
  ///
  /// * [String] symbols (required):
  Future<Map<String, String>?> getSectors(String symbols,) async {
    final response = await getSectorsWithHttpInfo(symbols,);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      return Map<String, String>.from(await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'Map<String, String>'),);

    }
    return null;
  }

  /// Search securities
  ///
  /// Search for securities using various filters
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [SecuritySearchRequestV1] securitySearchRequestV1 (required):
  Future<Response> searchWithHttpInfo(SecuritySearchRequestV1 securitySearchRequestV1,) async {
    // ignore: prefer_const_declarations
    final path = r'/api/v1/security/search';

    // ignore: prefer_final_locals
    Object? postBody = securitySearchRequestV1;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    const contentTypes = <String>['application/json'];


    return apiClient.invokeAPI(
      path,
      'POST',
      queryParams,
      postBody,
      headerParams,
      formParams,
      contentTypes.isEmpty ? null : contentTypes.first,
    );
  }

  /// Search securities
  ///
  /// Search for securities using various filters
  ///
  /// Parameters:
  ///
  /// * [SecuritySearchRequestV1] securitySearchRequestV1 (required):
  Future<List<SecurityDTOV1>?> search(SecuritySearchRequestV1 securitySearchRequestV1,) async {
    final response = await searchWithHttpInfo(securitySearchRequestV1,);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      final responseBody = await _decodeBodyBytes(response);
      return (await apiClient.deserializeAsync(responseBody, 'List<SecurityDTOV1>') as List)
        .cast<SecurityDTOV1>()
        .toList(growable: false);

    }
    return null;
  }
}
