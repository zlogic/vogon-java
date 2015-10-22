/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.jboss;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;
import org.webjars.WebJarAssetLocator;
import org.webjars.urlprotocols.UrlProtocolHandler;

//TODO: delete this class once this is integrated into WebJarAssetLocator or available as a Maven artifact
/**
 * Class for resolving JBoss VFS URLs in WebJarAssetLocator. Copied from
 * https://github.com/mwanji/webjars-locator-jboss-vfs
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class JbossUrlProtocolHandler implements UrlProtocolHandler {

	/**
	 * Returns true if this ProcotolHandler accepts a protocol
	 *
	 * @param protocol the protocol type
	 * @return true if protocol is "vfs"
	 */
	@Override
	public boolean accepts(String protocol) {
		return "vfs".equals(protocol);
	}

	/**
	 * Returns the set of asset paths
	 *
	 * @param url the URL to search
	 * @param filterExpr the filter to apply
	 * @param classLoaders classloaders to search in
	 * @return the set of asset paths
	 */
	@Override
	public Set<String> getAssetPaths(URL url, final Pattern filterExpr, ClassLoader... classLoaders) {
		try {
			final VirtualFile virtualFile = VFS.getChild(url.toURI());
			List<VirtualFile> children = virtualFile.getChildrenRecursively(new VirtualFileFilter() {
				@Override
				public boolean accepts(VirtualFile file) {
					if (file.isDirectory()) {
						return false;
					}
					int prefixIndex = file.getPathName().indexOf(WebJarAssetLocator.WEBJARS_PATH_PREFIX);
					if (prefixIndex == -1)
						return false;

					final String relativePath = file.getPathName().substring(prefixIndex);

					return file.isFile() && filterExpr.matcher(relativePath).matches();
				}
			});
			Set<String> assetPaths = new HashSet<String>();
			for (VirtualFile child : children)
				assetPaths.add(child.getPathName().substring(child.getPathName().indexOf(WebJarAssetLocator.WEBJARS_PATH_PREFIX)));
			return assetPaths;
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
