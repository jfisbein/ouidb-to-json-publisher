package com.sputnik.ouidb.entity;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.DecompressingEntity;
import org.apache.http.client.entity.InputStreamFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@link org.apache.http.entity.HttpEntityWrapper} for handling bz2
 * Content Coded responses.
 */
public class Bz2DecompressingEntity extends DecompressingEntity {

    /**
     * Creates a new {@link Bz2DecompressingEntity} which will wrap the specified
     * {@link HttpEntity}.
     *
     * @param entity the non-null {@link HttpEntity} to be wrapped
     */
    public Bz2DecompressingEntity(HttpEntity entity) {
        super(entity, new InputStreamFactory() {

            @Override
            public InputStream create(InputStream instream) throws IOException {
                return new BZip2CompressorInputStream(instream);
            }

        });
    }

}
