package im.vinci.server.naturelang.domain;

public class RecordResponse {
	private int rc;
	private String text;
	private String rtext;
	private String service;
	private String operation;
	private Semantic semantic = new Semantic();
	public class Semantic{
		private Slots slots = new Slots();
		public class Slots{
			private String datetime;
			private String dateOrig;
			private String type;
			private String name;
			public String getDatetime() {
				return datetime;
			}
			public void setDatetime(String datetime) {
				this.datetime = datetime;
			}
			public String getDateOrig() {
				return dateOrig;
			}
			public void setDateOrig(String dateOrig) {
				this.dateOrig = dateOrig;
			}
			public String getType() {
				return type;
			}
			public void setType(String type) {
				this.type = type;
			}
			public String getName() {
				return name;
			}
			public void setName(String name) {
				this.name = name;
			}
		}
		public Slots getSlots() {
			return slots;
		}
		public void setSlots(Slots slots) {
			this.slots = slots;
		}
	}
	public int getRc() {
		return rc;
	}
	public void setRc(int rc) {
		this.rc = rc;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getRtext() {
		return rtext;
	}
	public void setRtext(String rtext) {
		this.rtext = rtext;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public Semantic getSemantic() {
		return semantic;
	}
	public void setSemantic(Semantic semantic) {
		this.semantic = semantic;
	}

}
