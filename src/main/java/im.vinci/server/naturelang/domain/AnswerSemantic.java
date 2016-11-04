package im.vinci.server.naturelang.domain;

import java.util.List;

public class AnswerSemantic {
    private String text;
    private List  list ;//返回的推荐集

    public List getList() {
		return list;
	}

	public void setList(List list) {
		this.list = list;
	}

	public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
