package com.openshift.internal.restclient;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpDestination;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.Origin;
import org.eclipse.jetty.client.api.Connection;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Request.FailureListener;
import org.eclipse.jetty.client.api.Request.RequestListener;
import org.eclipse.jetty.client.api.Request.SuccessListener;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.client.api.Response.ContentListener;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.spdy.api.DataInfo;
import org.eclipse.jetty.spdy.api.GoAwayResultInfo;
import org.eclipse.jetty.spdy.api.HeadersInfo;
import org.eclipse.jetty.spdy.api.PingResultInfo;
import org.eclipse.jetty.spdy.api.PushInfo;
import org.eclipse.jetty.spdy.api.ReplyInfo;
import org.eclipse.jetty.spdy.api.RstInfo;
import org.eclipse.jetty.spdy.api.SPDY;
import org.eclipse.jetty.spdy.api.Session;
import org.eclipse.jetty.spdy.api.SessionFrameListener;
import org.eclipse.jetty.spdy.api.SettingsInfo;
import org.eclipse.jetty.spdy.api.Stream;
import org.eclipse.jetty.spdy.api.StreamFrameListener;
import org.eclipse.jetty.spdy.api.SynInfo;
import org.eclipse.jetty.spdy.client.SPDYClient;
import org.eclipse.jetty.spdy.client.http.HttpClientTransportOverSPDY;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.Promise;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Before;
import org.junit.Test;

public class PortForwardingIntegrationTest {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testClient() throws Exception {
//		SPDYClient.Factory factory = new SPDYClient.Factory();
//		factory.start();
//		SPDYClient spdyClient = factory.newSPDYClient(SPDY.V3);
		HttpClientTransportOverHTTP transport = new HttpClientTransportOverHTTP();
//		HttpClientTransportOverSPDY transport = new HttpClientTransportOverSPDY(spdyClient);
		HttpClient client = new HttpClient(transport, new SslContextFactory(true));
		transport.setHttpClient(client);
		client.start();
		URI uri = new URI("https://localhost:8443/api/v1/namespaces/test/pods/hello-openshift/portforward");
		Response.Listener responseListener = new Response.Listener.Adapter(){
			
			
			@Override
			public void onContent(Response response, ByteBuffer content, Callback callback) {
				System.out.println("response.onContent with callback");
			}

			@Override
			public void onBegin(Response response) {
				System.out.println("response.onBegin");
			}

			@Override
			public void onComplete(Result result) {
				System.out.println("response.onComplete");
				super.onComplete(result);
			}

			@Override
			public void onContent(Response response, ByteBuffer content) {
				System.out.println("response.onContent");
				byte [] buffer = new byte [content.capacity()];
				try {
					System.out.println(new String(buffer, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Response response, Throwable failure) {
				System.out.println("response.onFailure");
				// TODO Auto-generated method stub
				super.onFailure(response, failure);
			}

			@Override
			public void onSuccess(Response response) {
				System.out.println("response.onSuccess");
				System.out.println(response.getReason());
				System.out.println(response.getHeaders());
				System.out.println(response.getStatus());
			}
			
		};
		Request.Listener reqListener = new Request.Listener.Adapter() {

			@Override
			public void onQueued(Request request) {
				System.out.println("request.onQueued");
			}

			@Override
			public void onBegin(Request request) {
				System.out.println("request.onbegin");
			}

			@Override
			public void onCommit(Request request) {
				System.out.println("request.oncommit");
			}

			@Override
			public void onContent(Request request, ByteBuffer content) {
				System.out.println("request.oncontent");
			}

			@Override
			public void onFailure(Request request, Throwable failure) {
				System.out.println("request.onfailure");
			}

			@Override
			public void onSuccess(Request request) {
				System.out.println("request.onsuccess");
			}
			
		};
		Request request = client.newRequest(uri);
		request
			.header("Authorization", "Bearer wsx_5_Qkczml7IfLYFYhYjwmiFk6cAeMXLXs3s1WdCI")
			.header("Connection", "Upgrade")
			.header("Upgrade", "SPDY/3.1")
			.followRedirects(true)
			.onRequestBegin(reqListener)
			.onRequestCommit(reqListener)
			.onRequestContent(reqListener)
			.onRequestFailure(reqListener)
			.onRequestSuccess(reqListener)
			.onResponseBegin(responseListener)
			.onResponseContent(responseListener)
			.onResponseFailure(responseListener)
			.onResponseSuccess(responseListener);
		request.send(new CompleteListener() {
			
			@Override
			public void onComplete(Result result) {
				System.out.println("result.oncomplete");
//				System.out.println(result.g);
				
			}
		});
		while(System.in.read() != -1) {
			Thread.sleep(1000);
		}
	}

//	@Test
//	public void test() throws Exception {
//		// Start a SPDYClient factory shared among all SPDYClient instances
//		SPDYClient.Factory clientFactory = new SPDYClient.Factory();
//		clientFactory.start();
//		 
//		// Create one SPDYClient instance
//		SPDYClient client = clientFactory.newSPDYClient(SPDY.V3);
//		 
//		// Obtain a Session instance to send data to the server that listens on port 8181
//		Session session = client.connect(new InetSocketAddress("localhost", 8443), new SessionFrameListener() {
//
//			@Override
//			public StreamFrameListener onSyn(Stream stream, SynInfo synInfo) {
//				System.out.println("onSysn");
//				return null;
//			}
//
//			@Override
//			public void onRst(Session session, RstInfo rstInfo) {
//				System.out.println("onRst");
//			}
//
//			@Override
//			public void onSettings(Session session, SettingsInfo settingsInfo) {
//				System.out.println("onSettings");
//			}
//
//			@Override
//			public void onPing(Session session, PingResultInfo pingResultInfo) {
//				System.out.println("onPing");
//			}
//
//			@Override
//			public void onGoAway(Session session, GoAwayResultInfo goAwayResultInfo) {
//				System.out.println("onGoAway");
//			}
//
//			@Override
//			public void onFailure(Session session, Throwable x) {
//				System.out.println("onFailur");
//				x.printStackTrace(System.err);
//			}
//			
//		});
////		.get(5, TimeUnit.SECONDS);
//		 
//		// Sends SYN_STREAM and DATA to the server
//		Fields headers = new Fields();
//		headers.add("Authorization", "Bearer dvLiJ9ssLYLSf11VocMBoqyrAQAFTHQCefVjfER_fmw");
//		headers.add("GET", "/api/v1/namespaces/test/pods/hello-openshift/portforward HTTP/1.1");
//		SynInfo info = new SynInfo(headers, false);
//		StreamFrameListener listener = new StreamFrameListener() {
//
//			@Override
//			public void onReply(Stream stream, ReplyInfo replyInfo) {
//				System.out.println("onreply");
//			}
//
//			@Override
//			public void onHeaders(Stream stream, HeadersInfo headersInfo) {
//				System.out.println("onheaders");
//				
//			}
//
//			@Override
//			public StreamFrameListener onPush(Stream stream, PushInfo pushInfo) {
//				System.out.println("onPush");
//				return null;
//			}
//
//			@Override
//			public void onData(Stream stream, DataInfo dataInfo) {
//				System.out.println("onData");
//				
//			}
//
//			@Override
//			public void onFailure(Stream stream, Throwable x) {
//				System.out.println("onFailure");
//			}
//			
//		};
//		Stream stream = session.syn(info, listener);
//		Fields getHeaders = new Fields();
////		getHeaders.add("GET", "/v1/namespaces/test/pods/hello-openshift");
//		stream.reply(new ReplyInfo(headers, false), new Callback() {
//
//			@Override
//			public void failed(Throwable arg0) {
//				arg0.printStackTrace(System.err);
//			}
//
//			@Override
//			public void succeeded() {
//				System.out.println("succeeded");
//			}
//			
//		});
//	}

}
