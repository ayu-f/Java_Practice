public class Log {
    public enum LogItems {
        LOG_INVALID_ARGUMENT("Invalid arguments"),
        LOG_FAILED_TO_READ("Failed to read"),
        LOG_FAILED_TO_WRITE("Failed to write"),
        LOG_CONFIG_GRAMMAR_ERROR("problem with config grammar"),
        LOG_CONFIG_SEMANTIC_ERROR("problem with semantic of config"),
        LOG_ERROR("Error");

        private String title;

        LogItems(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }
}
