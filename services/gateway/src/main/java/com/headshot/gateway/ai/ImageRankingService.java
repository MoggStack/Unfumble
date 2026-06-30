package com.headshot.gateway.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Uses a vision-capable AI model (via Spring AI ChatClient) to pick the best
 * photo from a list of uploaded images.
 *
 * <p><b>Current state:</b> stub implementation — always returns index 0.
 * The real ChatClient call will be wired in once the OPENAI_API_KEY is configured.</p>
 *
 * <p><b>Planned behaviour:</b> send each image URL to GPT-4o with a structured prompt
 * asking it to evaluate photo quality, lighting, face clarity, and background,
 * then return the index of the best candidate.</p>
 */
@Service
public class ImageRankingService {

    private static final Logger log = LoggerFactory.getLogger(ImageRankingService.class);

    // TODO: inject ChatClient here once OPENAI_API_KEY is configured
    // private final ChatClient chatClient;

    /**
     * Ranks the provided image keys and returns the index of the best one.
     *
     * @param imageKeys list of MinIO object keys for the uploaded photos
     * @param useCase   the intended use (e.g. PASSPORT, LINKEDIN) — affects scoring criteria
     * @return index of the best image in {@code imageKeys}
     */
    public int rankImages(List<String> imageKeys, String useCase) {
        if (imageKeys == null || imageKeys.isEmpty()) {
            throw new IllegalArgumentException("imageKeys must not be empty");
        }

        // ── STUB ─────────────────────────────────────────────────────────────
        // Replace this block with the real Spring AI ChatClient call.
        // Example prompt structure to implement:
        //
        //   "You are a professional photo selection assistant.
        //    Given these [N] photos, select the best one for a [useCase] photo.
        //    Consider: face visibility, background, lighting, sharpness.
        //    Reply with only the integer index (0-based) of the best photo."
        //
        log.info("[STUB] ImageRankingService.rankImages called with {} images for useCase {}. " +
                 "Returning index 0 until ChatClient is configured.", imageKeys.size(), useCase);
        return 0;
        // ── END STUB ──────────────────────────────────────────────────────────
    }
}
