package lti.gateway;

import java.util.Map;
 
public class DummySmsService implements SmsService {

	DummySmsService(Map<String, String> config) {

	}

	@Override
	public void send(String phoneNumber, String message) {

	}

}