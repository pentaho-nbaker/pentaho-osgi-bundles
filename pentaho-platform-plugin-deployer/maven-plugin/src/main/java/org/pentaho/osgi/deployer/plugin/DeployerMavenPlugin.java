package org.pentaho.osgi.deployer.plugin;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.pentaho.osgi.platform.plugin.deployer.PlatformPluginBundlingURLConnection;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.impl.JSONUtil;
import org.pentaho.osgi.platform.plugin.deployer.impl.handlers.BlueprintFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.impl.handlers.ManifestFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.impl.handlers.ManifestHandler;
import org.pentaho.osgi.platform.plugin.deployer.impl.handlers.PluginLibraryFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.impl.handlers.SpringFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlContentTypeHandler;
import org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlExternalResourcesHandler;
import org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlLifecycleListenerHandler;
import org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlPluginIdHandler;
import org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nbaker on 10/19/16.
 */
@Mojo( name = "deployer" )
public class DeployerMavenPlugin extends AbstractMojo {

  @Parameter( property = "deployer.source"  )
  private String source;

  @Parameter( property = "deployer.destination" )
  private String destination;

  public void execute() throws MojoExecutionException {
    try {
      File sourceFile = new File( source );
      if ( !sourceFile.exists() ) {
        throw new MojoFailureException( "Source artifact does not exist" );
      }

      File destinationFile = new File( destination );
      destinationFile.getParentFile().mkdirs();
      destinationFile.createNewFile();

      URL fileUrl = sourceFile.getAbsoluteFile().toURL();

      PluginXmlStaticPathsHandler pluginXmlStaticPathsHandler = new PluginXmlStaticPathsHandler();
      JSONUtil jsonUtil = new JSONUtil();
      pluginXmlStaticPathsHandler.setJsonUtil( jsonUtil );
      PluginXmlExternalResourcesHandler pluginXmlExternalResourcesHandler = new PluginXmlExternalResourcesHandler();
      pluginXmlExternalResourcesHandler.setJsonUtil( jsonUtil );
      List<PluginFileHandler> pluginFileHandlers = Arrays.asList(
        new BlueprintFileHandler(),
        new ManifestFileHandler(),
        new ManifestHandler(),
        new PluginLibraryFileHandler(),
        new PluginXmlContentTypeHandler(),
        pluginXmlExternalResourcesHandler,
        new PluginXmlLifecycleListenerHandler(),
        new PluginXmlPluginIdHandler(),
        pluginXmlStaticPathsHandler,
        new SpringFileHandler()
      );

      PlatformPluginBundlingURLConnection connection =
        new PlatformPluginBundlingURLConnection( fileUrl, pluginFileHandlers );

      connection.connect();
      InputStream inputStream = connection.getInputStream();
      FileOutputStream outputStream = new FileOutputStream( destinationFile );
      IOUtils.copy( inputStream, outputStream );
      outputStream.close();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }
}
