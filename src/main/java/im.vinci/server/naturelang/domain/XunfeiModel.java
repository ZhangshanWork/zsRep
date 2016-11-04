package im.vinci.server.naturelang.domain;

import java.io.Serializable;

public class XunfeiModel implements Serializable{
	private String flag;
	private String text;
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
}
