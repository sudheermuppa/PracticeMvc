package com.loyaltymethods.fx.meta;

/**
 * Some things are very specific to each entity. We use a plug-in appraoch to address those things.
 * 
 * @author Emil
 *
 */
public interface EntitySpecifics {
	public void processEntitySpecifics(MetaGenerator metaGen);
}
