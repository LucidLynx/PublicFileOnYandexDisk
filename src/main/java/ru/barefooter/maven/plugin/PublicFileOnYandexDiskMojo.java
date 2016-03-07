package ru.barefooter.maven.plugin;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * Created with IntelliJ IDEA.
 * User: Stas
 * Date: 06.03.16
 * Time: 18:27
 */
@Mojo(name = "public-file", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class PublicFileOnYandexDiskMojo extends AbstractMojo {

    @Parameter(required = true)
    private String url;

    @Parameter(required = true)
    private String serverId;

    @Parameter(defaultValue = "${settings}", readonly = true)
    protected Settings settings;

    @Component(role = SecDispatcher.class, hint = "mojo")
    private SecDispatcher secDispatcher;

    @Parameter(defaultValue = "${project.name}", readonly = true)
    protected String projectName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        publicOnYandex(url);
    }

    private void publicOnYandex(String url) {
        PropPatchMethod method = null;
        try {
            Server server = settings.getServer(serverId);
            String username = server.getUsername();
            String password = secDispatcher.decrypt(server.getPassword());
            HttpClient client = new HttpClient();
            Credentials credentials = new UsernamePasswordCredentials(username, password);
            client.getState().setCredentials(AuthScope.ANY, credentials);

            DavPropertySet davPropertySet = new DavPropertySet();
            Namespace namespace = Namespace.getNamespace("urn:yandex:disk:meta");
            davPropertySet.add(new DefaultDavProperty<Boolean>("public_url", true, namespace));

            method = new PropPatchMethod(url, davPropertySet, new DavPropertyNameSet());
            client.executeMethod(method);
            if (method.succeeded()) {
                MultiStatus multiStatus = method.getResponseBodyAsMultiStatus();
                MultiStatusResponse response = multiStatus.getResponses()[0];
                DavPropertySet propertySet = response.getProperties(HttpStatus.SC_OK);
                DavProperty<?> property = propertySet.get("public_url", namespace);
                if (property != null) {
                    String publicUrl = (String) property.getValue();
                    getLog().info("Link: " + publicUrl);
                    String data = projectName + " — " + publicUrl;
                    StringSelection stringSelection = new StringSelection(data);
                    Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clpbrd.setContents(stringSelection, null);
                    String message = "Название проекта и ссылка скопированы в буфер обмена\n" + publicUrl;
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
                    JDialog dialog = pane.createDialog(projectName);
                    dialog.setAlwaysOnTop(true);
                    dialog.setVisible(true);
                }
            }
        } catch (Exception e) {
            getLog().error(e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }
}
