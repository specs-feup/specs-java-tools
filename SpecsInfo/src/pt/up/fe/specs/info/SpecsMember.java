package pt.up.fe.specs.info;

public class SpecsMember {

	private String Name;
	private String Affiliation;
	private String Position;
	private String Context;
	private String CurrentStatus;
	// optional
	private String VisitingPeriod;
	private String FirstJob;
	private String Status;
	private String ORCID;
	private String DBLP;
	private String ResearchGate;
	private String SchoolarGoogle;
	private String Linkedin;
	private String Twitter;
	private String WebPage;
	private String Email;
	private String FirstJobMsc;
	private String PublicKey;
	private String Supervisor;

	public SpecsMember(String name, String affiliation, String position, String context, String currentStatus) {

		Name = name;
		Affiliation = affiliation;
		Position = position;
		Context = context;
		CurrentStatus = currentStatus;
	}

	public void setName(String name) {
		Name = name;
	}

	public void setAffiliation(String affiliation) {
		Affiliation = affiliation;
	}

	public void setPosition(String position) {
		Position = position;
	}

	public void setContext(String context) {
		Context = context;
	}

	public void setCurrentStatus(String currentStatus) {
		CurrentStatus = currentStatus;
	}

	public void setVisitingPeriod(String visitingPeriod) {
		VisitingPeriod = visitingPeriod;
	}

	public void setFirstJob(String firstJob) {
		FirstJob = firstJob;
	}

	public void setStatus(String status) {
		Status = status;
	}

	public void setORCID(String oRCID) {
		ORCID = oRCID;
	}

	public void setDBLP(String dBLP) {
		DBLP = dBLP;
	}

	public void setResearchGate(String researchGate) {
		ResearchGate = researchGate;
	}

	public void setSchoolarGoogle(String schoolarGoogle) {
		SchoolarGoogle = schoolarGoogle;
	}

	public void setLinkedin(String linkedin) {
		Linkedin = linkedin;
	}

	public void setTwitter(String twitter) {
		Twitter = twitter;
	}

	public void setWebPage(String webPage) {
		WebPage = webPage;
	}

	public void setEmail(String email) {
		Email = email;
	}

	public void setFirstJobMsc(String firstJobMsc) {
		FirstJobMsc = firstJobMsc;
	}

	public void setPublicKey(String publicKey) {
		PublicKey = publicKey;
	}

	public void setSupervisor(String supervisor) {
		Supervisor = supervisor;
	}
}
