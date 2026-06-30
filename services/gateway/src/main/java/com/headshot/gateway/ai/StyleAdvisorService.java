package com.headshot.gateway.ai;

import com.headshot.gateway.job.Job;
import org.springframework.stereotype.Service;

/**
 * Builds a use-case-specific style description string that is forwarded to the
 * Python AI Engine alongside the chosen image.
 *
 * <p>The Python side owns the actual prompt engineering and image-editing API call —
 * this class simply translates the Java UseCase enum into a human-readable instruction
 * that the Python engine can embed directly into its prompt template.</p>
 */
@Service
public class StyleAdvisorService {

    /**
     * Returns a plain-English description of the style requirements for the given use-case.
     * This string is sent as part of the job payload to the Python AI engine.
     *
     * @param useCase the target use-case selected by the user
     * @return style requirements description
     */
    public String describeStyle(Job.UseCase useCase) {
        return switch (useCase) {
            case PASSPORT -> """
                    Professional passport photo: plain white or off-white background, \
                    neutral front-facing expression, shoulders visible, \
                    no shadows, even lighting, no accessories obstructing face.""";

            case LINKEDIN -> """
                    Professional LinkedIn headshot: clean, blurred or neutral background, \
                    friendly and approachable expression, business-casual attire, \
                    good lighting, cropped at chest level.""";

            case INSTAGRAM -> """
                    Lifestyle Instagram profile photo: vibrant and eye-catching, \
                    natural or aesthetically pleasing background, \
                    warm and engaging expression, modern colour grading.""";

            case JOB_APPLICATION -> """
                    Formal job application photo: neutral or light-grey background, \
                    professional attire, confident and composed expression, \
                    sharp focus, minimal retouching.""";
        };
    }
}
