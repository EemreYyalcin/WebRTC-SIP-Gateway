package sipserver.com.timer.control;

import sipserver.com.domain.Extension;
import sipserver.com.timer.Task;

public class RegisterControl extends Task {

	private Extension extension;

	public RegisterControl(Extension extension) {
		super(extension.getExpiresTime(), extension.getExten());
		setExtension(extension);
	}

	@Override
	public boolean isRegistered() {
		return getExtension().isOnline();
	}

	@Override
	public void endTask() {
		getExtension().setOnline(false);
	}

	public Extension getExtension() {
		return extension;
	}

	public void setExtension(Extension extension) {
		this.extension = extension;
	}

}
