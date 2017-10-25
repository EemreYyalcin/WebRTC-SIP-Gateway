package sipserver.com.executer.sip.transaction;

import java.net.InetAddress;
import java.util.Objects;
import java.util.function.BooleanSupplier;

import javax.sip.header.CSeqHeader;
import javax.sip.message.Request;

import com.noyan.Base;

import sipserver.com.server.SipServerTransport;
import sipserver.com.service.util.GeneraterService;

public class Transaction implements Base {

	private Request request;
	private InetAddress address;
	private int port = 5060;
	private String callId;

	private String cseqName;
	private long cseqNumber = 1;

	private SipServerTransport transport;

	private BooleanSupplier setCseqValue = () -> {
		if (Objects.isNull(request)) {
			return false;
		}
		CSeqHeader cseqHeader = (CSeqHeader) request.getHeader(CSeqHeader.NAME);
		if (Objects.isNull(cseqHeader)) {
			return false;
		}
		setCseqNumber(cseqHeader.getSeqNumber());
		setCseqName(cseqHeader.getMethod());
		return true;
	};

	public Transaction(Request request, InetAddress address, int port, SipServerTransport transport) {
		setRequest(request);
		setAddress(address);
		setPort(port);
		if (!setCseqValue.getAsBoolean()) {
			setCseqName(request.getMethod());
			setCseqNumber(1);
			setCallId(GeneraterService.getUUid(10));
		}
		setTransport(transport);
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getCseqName() {
		return cseqName;
	}

	public void setCseqName(String cseqName) {
		this.cseqName = cseqName;
	}

	public long getCseqNumber() {
		return cseqNumber;
	}

	public void setCseqNumber(long cseqNumber) {
		this.cseqNumber = cseqNumber;
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public SipServerTransport getTransport() {
		return transport;
	}

	public void setTransport(SipServerTransport transport) {
		this.transport = transport;
	}

}
