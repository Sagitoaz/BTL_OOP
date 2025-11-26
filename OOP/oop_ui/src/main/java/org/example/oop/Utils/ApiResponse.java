package org.example.oop.Utils;

/**
 * API RESPONSE WRAPPER
 * Type-safe response wrapper for API calls with success/error handling
 */
public class ApiResponse<T> {

     private final boolean success;
     private final T data;
     private final String errorMessage;
     private final int statusCode;

     private ApiResponse(boolean success, T data, String errorMessage, int statusCode) {
          this.success = success;
          this.data = data;
          this.errorMessage = errorMessage;
          this.statusCode = statusCode;
     }

     // FACTORY METHODS

     /**
      * Create successful response with data
      */
     public static <T> ApiResponse<T> success(T data) {
          return new ApiResponse<>(true, data, null, 200);
     }

     /**
      * Create successful response with status code
      */
     public static <T> ApiResponse<T> success(T data, int statusCode) {
          return new ApiResponse<>(true, data, null, statusCode);
     }

     /**
      * Create error response with message
      */
     public static <T> ApiResponse<T> error(String errorMessage) {
          return new ApiResponse<>(false, null, errorMessage, 500);
     }

     /**
      * Create error response with message and status code
      */
     public static <T> ApiResponse<T> error(String errorMessage, int statusCode) {
          return new ApiResponse<>(false, null, errorMessage, statusCode);
     }

     // GETTERS

     /**
      * Check if API call was successful
      */
     public boolean isSuccess() {
          return success;
     }

     /**
      * Check if API call failed
      */
     public boolean isError() {
          return !success;
     }

     /**
      * Get response data (null if error)
      */
     public T getData() {
          return data;
     }

     /**
      * Get error message (null if success)
      */
     public String getErrorMessage() {
          return errorMessage;
     }

     /**
      * Get HTTP status code
      */
     public int getStatusCode() {
          return statusCode;
     }

     // UTILITY METHODS

     /**
      * Get data or default value if error
      */
     public T getDataOrDefault(T defaultValue) {
          return success ? data : defaultValue;
     }

     /**
      * Get error message or default message
      */
     public String getErrorOrDefault(String defaultMessage) {
          return success ? defaultMessage : (errorMessage != null ? errorMessage : defaultMessage);
     }

     /**
      * Check if response has data
      */
     public boolean hasData() {
          return success && data != null;
     }

     @Override
     public String toString() {
          if (success) {
               return "ApiResponse{success=true, statusCode=" + statusCode +
                         ", data=" + (data != null ? data.toString() : "null") + "}";
          } else {
               return "ApiResponse{success=false, statusCode=" + statusCode +
                         ", error='" + errorMessage + "'}";
          }
     }
}