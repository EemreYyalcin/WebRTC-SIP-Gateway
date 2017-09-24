package sipserver.com.parameter.param;

import sipserver.com.executer.core.SipServerSharedProperties;

public class ExtensionParam {

		private boolean isDeletedExtension = false;
		private boolean isCheckRegister = false;
		private boolean keepRegisteredFlag = false;
		
		private boolean isRegisterResponseRecieved = false;
		private boolean isOptionsResponseRecieved = false;
		private int registerResponseCode = SipServerSharedProperties.errorResponseCode;
		private int optionsResponseCode = SipServerSharedProperties.errorResponseCode;
		
		
		
		public boolean isDeletedExtension() {
			return isDeletedExtension;
		}
		public void setDeletedExtension(boolean isDeletedExtension) {
			this.isDeletedExtension = isDeletedExtension;
		}
		public boolean isCheckRegister() {
			return isCheckRegister;
		}
		public void setCheckRegister(boolean isCheckRegister) {
			this.isCheckRegister = isCheckRegister;
		}
		public boolean isKeepRegisteredFlag() {
			return keepRegisteredFlag;
		}
		public void setKeepRegisteredFlag(boolean keepRegisteredFlag) {
			this.keepRegisteredFlag = keepRegisteredFlag;
		}
		public boolean isRegisterResponseRecieved() {
			return isRegisterResponseRecieved;
		}
		public void setRegisterResponseRecieved(boolean isRegisterResponseRecieved) {
			this.isRegisterResponseRecieved = isRegisterResponseRecieved;
		}
		public boolean isOptionsResponseRecieved() {
			return isOptionsResponseRecieved;
		}
		public void setOptionsResponseRecieved(boolean isOptionsResponseRecieved) {
			this.isOptionsResponseRecieved = isOptionsResponseRecieved;
		}
		public int getRegisterResponseCode() {
			return registerResponseCode;
		}
		public void setRegisterResponseCode(int registerResponseCode) {
			this.registerResponseCode = registerResponseCode;
		}
		public int getOptionsResponseCode() {
			return optionsResponseCode;
		}
		public void setOptionsResponseCode(int optionsResponseCode) {
			this.optionsResponseCode = optionsResponseCode;
		}
		
		
		
		
	
}
