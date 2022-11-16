package lti.gateway;

import org.jboss.logging.Logger;

import java.util.Map;
 
public class DummySmsService implements SmsService {

	private static final Logger LOG = Logger.getLogger(SmsServiceFactory.class);

	DummySmsService(Map<String, String> config) {
		LOG.warn(String.format("***** INIT DUMMY SERVICE *****"));
	}

	@Override
	public void send(String phoneNumber, String message) {
		LOG.warn(String.format("***** SENDING DUMMY MSG *****"));
	}

}