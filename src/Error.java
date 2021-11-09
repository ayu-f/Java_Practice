public class Error {
    enum ErrorCode {
        NO_ERROR(0),
        CONFIG_ERROR(1),
        CONFIG_SEMANTIC_ERROR(2),
        READ_ERROR(3),
        WRITE_ERROR(4),
        ENCODE_ERROR(5),
        DECODE_ERROR(6);

        private final int errorCode;
        ErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }
    }

    public static ErrorCode errNo = ErrorCode.NO_ERROR;

    /**
     * Update the error code
     */
    public static void UpdError(ErrorCode err) {
        errNo = err;
    }
}
