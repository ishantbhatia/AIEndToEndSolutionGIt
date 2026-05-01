package com.ai.factory.document;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reads text content from a specific document format.
 */
public interface DocumentReader {

    /** Returns true if this reader can handle the given file extension (e.g. ".pdf"). */
    boolean supports(String fileExtension);

    /** Extracts plain text from the input stream. */
    String read(InputStream inputStream) throws IOException;
}
