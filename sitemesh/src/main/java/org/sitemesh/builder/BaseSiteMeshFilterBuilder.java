package org.sitemesh.builder;

import org.sitemesh.config.PathMapper;
import org.sitemesh.webapp.WebAppContext;
import org.sitemesh.webapp.contentfilter.BasicSelector;
import org.sitemesh.webapp.contentfilter.Selector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.Filter;
import java.util.Arrays;
import java.util.Collection;

/**
 * Functionality for building a {@link org.sitemesh.webapp.BaseSiteMeshFilter}.
 * Inherits common functionality from {@link BaseSiteMeshBuilder}.
 *
 * <p>Clients should use the concrete {@link SiteMeshFilterBuilder} implementation.</p>
 *
 * @see BaseSiteMeshBuilder
 * @see org.sitemesh.webapp.BaseSiteMeshFilter
 *
 * @param <BUILDER> The type to return from the builder methods. Subclasses
 *                  should type this as their own class type.
 *
 * @author Joe Walnes
 */
public abstract class BaseSiteMeshFilterBuilder<BUILDER extends BaseSiteMeshBuilder>
        extends BaseSiteMeshBuilder<BUILDER, WebAppContext> {

    private Collection<String> mimeTypes;

    private PathMapper<Boolean> excludesMapper = new PathMapper<Boolean>();
    private Selector customSelector;

    /**
     * Create the SiteMesh Filter.
     */
    public abstract Filter create();

    /**
     * See {@link BaseSiteMeshFilterBuilder#setupDefaults()}.
     * In addition to the parent setup, this also calls {@link #setMimeTypes(String[])} with
     * <code>{"text/html"}</code>.
     */
    @Override
    protected void setupDefaults() {
        super.setupDefaults();
        setMimeTypes("text/html");
    }

    // --------------------------------------------------------------
    // Selector setup.

    /**
     * Add a path to be excluded by SiteMesh.
     */
    public BUILDER addExcludedPath(String exclude) {
        excludesMapper.put(exclude, true);
        return self();
    }

    // --------------------------------------------------------------
    // Selector setup.

    /**
     * Set MIME types that the Filter should intercept. The default
     * is <code>{"text/html"}</code>.
     *
     * <p>Note: The MIME types are ignored if {@link #setCustomSelector(Selector)} is called.</p>
     */
    public BUILDER setMimeTypes(String... mimeTypes) {
        this.mimeTypes = Arrays.asList(mimeTypes);
        return self();
    }

    /**
     * Set a custom {@link Selector}.
     *
     * <p>Note: If this is called, it will override any MIME types
     * passed to {@link #setMimeTypes(String[])} as these are specific
     * to the default Selector.</p>
     */
    public BUILDER setCustomSelector(Selector selector) {
        this.customSelector = selector;
        return self();
    }

    /**
     * Get configured {@link Selector}.
     */
    public Selector getSelector() {
        if (customSelector != null) {
            return customSelector;
        } else {
            String[] mimeTypesArray = mimeTypes.toArray(new String[mimeTypes.size()]);
            return new BasicSelector(mimeTypesArray) {
                // TODO: Move the exclusion functionality into BasicSelector.
                @Override
                public boolean shouldBufferForRequest(HttpServletRequest request) {
                    String requestPath = WebAppContext.getRequestPath(request);
                    return super.shouldBufferForRequest(request)
                            && excludesMapper.get(requestPath) == null;
                }
            };
        }
    }

}
