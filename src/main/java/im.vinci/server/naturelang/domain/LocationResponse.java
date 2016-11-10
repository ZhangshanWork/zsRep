package im.vinci.server.naturelang.domain;

public class LocationResponse {
	private String cityAddr;
	private String city;
	private String type;
	private String areaAddr;
	private String area;
	private String province;
	private String provinceAddr;
	public String getCityAddr() {
		return cityAddr;
	}
	public void setCityAddr(String cityAddr) {
		this.cityAddr = cityAddr;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getAreaAddr() {
		return areaAddr;
	}
	public void setAreaAddr(String areaAddr) {
		this.areaAddr = areaAddr;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getProvinceAddr() {
		return provinceAddr;
	}
	public void setProvinceAddr(String provinceAddr) {
		this.provinceAddr = provinceAddr;
	}
}
