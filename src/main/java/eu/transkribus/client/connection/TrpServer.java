package eu.transkribus.client.connection;

public enum TrpServer {
	Prod("https://transkribus.eu/TrpServer"),
	Test("https://transkribus.eu/TrpServerTesting"),
	Local("http://localhost:8080/TrpServerTesting");
	private final String uri;
	private TrpServer(String uri) {
		this.uri = uri;
	}
	public String getUriStr() {
		return uri;
	}
}