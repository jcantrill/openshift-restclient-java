package com.openshift.restclient.internal.fabric8io;

import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openshift.restclient.IClient;
import com.openshift.restclient.IOpenShiftWatchListener;
import com.openshift.restclient.IResourceFactory;
import com.openshift.restclient.ISSLCertificateCallback;
import com.openshift.restclient.IWatcher;
import com.openshift.restclient.OpenShiftException;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.UnsupportedVersionException;
import com.openshift.restclient.authorization.BasicAuthorizationStrategy;
import com.openshift.restclient.authorization.IAuthorizationContext;
import com.openshift.restclient.authorization.IAuthorizationDetails;
import com.openshift.restclient.authorization.IAuthorizationStrategy;
import com.openshift.restclient.authorization.IAuthorizationStrategyVisitor;
import com.openshift.restclient.authorization.TokenAuthorizationStrategy;
import com.openshift.restclient.capability.CapabilityVisitor;
import com.openshift.restclient.capability.ICapability;
import com.openshift.restclient.capability.resources.IFabric8ioResourceCapability;
import com.openshift.restclient.model.IList;
import com.openshift.restclient.model.IProject;
import com.openshift.restclient.model.IResource;
import com.openshift.restclient.model.user.IUser;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.ProjectRequest;
import io.fabric8.openshift.api.model.User;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.EditableOpenShiftConfig;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;

public class OpenShiftClientAdapter implements IClient {

	private static final Logger LOG = LoggerFactory.getLogger(OpenShiftClientAdapter.class);
	private final EditableOpenShiftConfig config;
	private ISSLCertificateCallback sslCertCallback;
	private OpenShiftClient client;
	private ResourceFactoryAdapter factory;
	private IAuthorizationStrategy authStrategy;

	public OpenShiftClientAdapter(String baseUrl, ISSLCertificateCallback sslCertCallback, String alias, X509Certificate cert, ResourceFactoryAdapter factory){
		this.factory = factory;
		this.sslCertCallback = sslCertCallback;
		config = new OpenShiftConfigBuilder()
				.withMasterUrl(baseUrl)
				.withTrustCerts(true) //config sslCertCallback
				.withConnectionTimeout(30)
				.build();
		client = new DefaultOpenShiftClient(config);
		factory.setClient(this);
	}
	
	public OpenShiftClient getOpenShiftClient(){
		return client;
	}
	
	@Override
	public <T extends ICapability> T getCapability(Class<T> capability) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean supports(Class<? extends ICapability> capability) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T extends ICapability, R> R accept(CapabilityVisitor<T, R> visitor, R unsupportedCapabililityValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAuthorizationContext getContext(String baseURL) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAuthorizationDetails getAuthorizationDetails(String baseURL) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSSLCertificateCallback(ISSLCertificateCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public IWatcher watch(String namespace, IOpenShiftWatchListener listener, String... kinds) {
		return null;
	}

	@Override
	public <T extends IResource> List<T> list(String kind) {
		return list(kind,"default");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IResource> List<T> list(String kind, String namespace) {
		return list(kind, namespace, Collections.EMPTY_MAP);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T extends IResource> List<T> list(String kind, String namespace, Map<String, String> labels) {
		try {
			KubernetesResourceList list = new OperationsBuilder(client, kind)
					.inNameSpace(namespace)
					.withLabels(labels)
					.list();
			return (List<T>)list.getItems()
					.stream()
					.map(i->factory.create((HasMetadata)i))
					.collect(Collectors.toList());
		}catch(Exception e) {
			throw new OpenShiftException(e, "Unable to list resource of kind %s", kind);
		}
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <T extends IResource> T get(String kind, String name, String namespace) {
		try {
			HasMetadata resource = new OperationsBuilder(client, kind)
										.inNameSpace(namespace)
										.withName(name)
										.get();
			LOG.debug("Retrieved {}", resource);
			return (T) factory.create(resource);
		} catch (Exception e) {
			throw new OpenShiftException(e, "Unable to get %s/%s/%s", namespace, kind, name);
		}
	}

	@Override
	public IList get(String kind, String namespace) {
		try {
			KubernetesResourceList list = (KubernetesResourceList) new OperationsBuilder(client, kind)
			.inNameSpace(namespace)
			.list();
			return (IList) factory.create(list);
		} catch (Exception e) {
			throw new OpenShiftException(e, "Unable to list %s/%s", namespace, kind);
		}
	}
	
	private IResource updateIResource(IResource instance, HasMetadata fabric8) {
		return instance.accept(new CapabilityVisitor<IFabric8ioResourceCapability, IResource>() {
			
			@Override
			public IResource visit(IFabric8ioResourceCapability capability) {
				capability.setResource(fabric8);
				return instance;
			}
		}, null);
	}

	private HasMetadata getFabric8Resource(IResource instance) {
		return instance.accept(new CapabilityVisitor<IFabric8ioResourceCapability, HasMetadata>() {
			
			@Override
			public HasMetadata visit(IFabric8ioResourceCapability capability) {
				return capability.getResource();
			}
		}, null);
	}

	private ProjectRequest createProjectRequest(IProject project) {
		return project.accept(new CapabilityVisitor<IFabric8ioResourceCapability, ProjectRequest>() {

			@Override
			public ProjectRequest visit(IFabric8ioResourceCapability capability) {
				Project resource = (Project) capability.getResource();
				ProjectRequest request = new ProjectRequest();
				request.setMetadata(new ObjectMeta());
				request.setApiVersion(resource.getApiVersion());
				request.getMetadata().setAnnotations(resource.getMetadata().getAnnotations());
				request.getMetadata().setName(resource.getMetadata().getName());
				return request;
			}
		}, null);
	}

	@Override
	public <T extends IResource> T create(String kind, String namespace, String name, String subresource,
			IResource payload) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends IResource> T create(T instance) {
		return create(instance, instance.getNamespace());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IResource> T create(T instance, String namespace) {
		LOG.debug("Creating {}", instance.toJson());
		HasMetadata resource = null;
		if(ResourceKind.PROJECT.equals(instance.getKind())){
			resource = createProjectRequest((IProject) instance);
		}else {
			resource = getFabric8Resource(instance);
		}
		LOG.debug("Creating fabric8 resource {}", resource);
		try {
			HasMetadata created = new OperationsBuilder(client, resource.getKind())
									.inNameSpace(namespace)
									.create(resource);
			LOG.debug("Created {}", created);
			if(ResourceKind.PROJECT.equals(instance.getKind())) {
				resource = client.projects().withName(resource.getMetadata().getName()).get();
			}
			return (T) updateIResource(instance, created);
		} catch (Exception e) {
			LOG.error(String.format("Error Creating %s", resource), e);
			try {
				throw new OpenShiftException(e, "Unable to create %s", new ObjectMapper().writeValueAsString(resource));
			} catch (JsonProcessingException e1) {
				throw new OpenShiftException(e, "Unable to create %s", resource);
			}
		}
	}

	@Override
	public Collection<IResource> create(IList list, String namespace) {
		List<IResource> results = new ArrayList<IResource>(list.getItems().size());
		for (IResource resource : list.getItems()) {
			try{
				results.add(create(resource, namespace));
			}catch(OpenShiftException e){
				if(e.getStatus() != null){
					results.add(e.getStatus());
				}else{
					throw e;
				}
			}
		}
		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IResource> T update(T resource) {
		LOG.debug("Updating {}", resource);
		try {
			HasMetadata updated = new OperationsBuilder(client, resource.getKind())
			.inNameSpace(resource.getNamespace())
			.update(getFabric8Resource(resource));
			return (T) updateIResource(resource, updated);
		}catch(Exception e) {
			LOG.error(String.format("Error Updating %s", resource), e);
			throw new OpenShiftException(e, "Unable to update %s", resource.toJson());
		}
	}

	@Override
	public <T extends IResource> void delete(T resource) {
		LOG.debug("Deleting {}", resource);
		try {
			new OperationsBuilder(client, resource.getKind())
				.inNameSpace(resource.getNamespace())
				.delete(getFabric8Resource(resource));
		}catch(Exception e) {
			LOG.error(String.format("Error Deleting %s", resource), e);
			throw new OpenShiftException(e, "Unable to delete %s", resource.toJson());
		}
	}

	@Override
	public URL getBaseURL() {
		return client.getOpenshiftUrl();
	}

	@Override
	public String getResourceURI(IResource resource) {
		return getFabric8Resource(resource).getMetadata().getSelfLink();
	}

	@Override
	public String getOpenShiftAPIVersion() throws UnsupportedVersionException {
		return client.getApiVersion();
	}

	@Override
	public void setAuthorizationStrategy(IAuthorizationStrategy strategy) {
		this.authStrategy = strategy;
		config.setUsername(strategy.getUsername());
		strategy.accept(new IAuthorizationStrategyVisitor() {
			
			@Override
			public void visit(TokenAuthorizationStrategy strategy) {
				config.setOauthToken(strategy.getToken());
			}
			
			@Override
			public void visit(BasicAuthorizationStrategy strategy) {
				config.setPassword(strategy.getPassword());
				config.setOauthToken(strategy.getToken());
			}
		});
	}

	@Override
	public IAuthorizationStrategy getAuthorizationStrategy() {
		return this.authStrategy;
	}

	@Override
	public IResourceFactory getResourceFactory() {
		return this.factory;
	}

	@Override
	public IUser getCurrentUser() {
		User user = client.users().withName("~").get();
		return factory.create(user);
	}

}
