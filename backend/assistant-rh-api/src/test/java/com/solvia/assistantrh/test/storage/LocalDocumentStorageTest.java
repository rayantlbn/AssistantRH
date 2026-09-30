package com.solvia.assistantrh.test.storage;

import com.solvia.assistantrh.storage.LocalDocumentStorage;
import com.solvia.assistantrh.storage.StoredDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalDocumentStorageTest {

    private static final byte[] CONTENT = "%PDF-1.4 contenu".getBytes(StandardCharsets.US_ASCII);

    @TempDir
    Path root;

    @Test
    void store_writesUnderGeneratedUuidName_thenLoadAndDelete() throws Exception {
        LocalDocumentStorage storage = new LocalDocumentStorage(root.toString());

        StoredDocument stored = storage.store(12L, new ByteArrayInputStream(CONTENT));

        assertThat(stored.path()).matches("candidates/12/[0-9a-f-]{36}\\.pdf");
        assertThat(stored.size()).isEqualTo(CONTENT.length);
        assertThat(storage.load(stored.path()).getContentAsByteArray()).isEqualTo(CONTENT);

        storage.delete(stored.path());
        assertThat(Files.exists(root.resolve(stored.path()))).isFalse();
    }

    @Test
    void twoFilesForTheSameCandidate_neverCollide() {
        LocalDocumentStorage storage = new LocalDocumentStorage(root.toString());

        String first = storage.store(12L, new ByteArrayInputStream(CONTENT)).path();
        String second = storage.store(12L, new ByteArrayInputStream(CONTENT)).path();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void pathOutsideStorage_isRejected() {
        LocalDocumentStorage storage = new LocalDocumentStorage(root.toString());

        assertThatThrownBy(() -> storage.load("../../etc/passwd")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> storage.delete("../outside.pdf")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deletingMissingFile_isNotAnError() {
        LocalDocumentStorage storage = new LocalDocumentStorage(root.toString());

        assertThatCode(() -> storage.delete("candidates/1/absent.pdf")).doesNotThrowAnyException();
    }
}
