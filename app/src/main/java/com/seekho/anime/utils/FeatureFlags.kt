package com.seekho.anime.utils

/**
 * Feature flags for legal compliance and app configuration
 * ideally these would be set via build config or remote config
 */
object FeatureFlags {

    /**
     * Controls whether profile/character images can be displayed
     * Set to false if legal requirements prohibit showing profile images
     *
     * When false:
     * - Character images will be hidden
     * - Anime posters will be hidden
     * - Layouts will adjust to text-only display
     * - No image loading will be attempted
     */
    const val ENABLE_PROFILE_IMAGES = true // Set to false due to legal requirements

    /**
     * Reason for disabling images (for documentation)
     */
    const val PROFILE_IMAGES_DISABLED_REASON = "Legal compliance - Profile images disabled"
}

