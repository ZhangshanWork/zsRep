package im.vinci.server.naturelang.domain;

public class Response_guide {
	private int rc;
	private String text;
	private String rtext;
	private String service;
	private String operation;
	private Semantic semantic = new Semantic();
	public class Semantic{
		private Slots slots = new Slots();
		public class Slots{
			private boolean guide;
			private Loc startLoc = new Loc();
			private Loc endLoc = new Loc();
			public Loc getStartLoc() {
				return startLoc;
			}
			public void setStartLoc(Loc startLoc) {
				this.startLoc = startLoc;
			}
			public Loc getEndLoc() {
				return endLoc;
			}
			public void setEndLoc(Loc endLoc) {
				this.endLoc = endLoc;
			}
			public boolean isGuide() {
				return guide;
			}
			public void setGuide(boolean guide) {
				this.guide = guide;
			}
		}
		public Slots getSlots() {
			return slots;
		}
		public void setSlots(Slots slots) {
			this.slots = slots;
		}
	}
	public class Loc{
		private String type;
		private String province;
		private String city;
		private String citycode;
		private String area;
		private String poi;
		private String keyword;
		private String location;
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getProvince() {
			return province;
		}
		public void setProvince(String province) {
			this.province = province;
		}
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		public String getCitycode() {
			return citycode;
		}
		public void setCitycode(String citycode) {
			this.citycode = citycode;
		}
		public String getArea() {
			return area;
		}
		public void setArea(String area) {
			this.area = area;
		}
		public String getPoi() {
			return poi;
		}
		public void setPoi(String poi) {
			this.poi = poi;
		}
		public String getKeyword() {
			return keyword;
		}
		public void setKeyword(String keyword) {
			this.keyword = keyword;
		}
		public String getLocation() {
			return location;
		}
		public void setLocation(String location) {
			this.location = location;
		}
	}
	public int getRc() {
		return rc;
	}
	public void setRc(int rc) {
		this.rc = rc;
	}
	public String getRtext() {
		return rtext;
	}
	public void setRtext(String rtext) {
		this.rtext = rtext;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
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
