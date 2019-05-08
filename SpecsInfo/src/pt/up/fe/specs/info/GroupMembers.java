/**
 * Copyright 2019 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package pt.up.fe.specs.info;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.api.services.sheets.v4.model.ValueRange;

public class GroupMembers {

	private final String spreadsheetId;
	private final File credentials;

	public GroupMembers(String spreadsheetId, File credentials) {
		this.spreadsheetId = spreadsheetId;
		this.credentials = credentials;
	}

	private void setField(int index, Consumer<String> func, List<?> row) {
		
		if (row.size() > index) {
			
			String value = (String) row.get(index);
			if (!value.isEmpty()) {
				func.accept(value);
			}
		}
	}
	
	public List<File> collectInformation() {
		List<File> infoFiles = new ArrayList<>();

		SpecsSheets service = GoogleSheets.getSpecsSheets(spreadsheetId, credentials);

		ValueRange response = service.getValueRange("Members!B3:T");
		List<List<Object>> values = response.getValues();
		if (values == null || values.isEmpty()) {
			throw new RuntimeException("No data found.");
		}
		List<SpecsMember> members = new ArrayList<>();

		System.out.println("\nMembers test");
		for (List<?> row : values) {

			if(row.size() == 0) {
				continue;
			}
			
			String name = (String) row.get(0);
			String affiliation = (String) row.get(1);
			String position = (String) row.get(2);
			String context = (String) row.get(4);
			String currentStatus = (String) row.get(6);

			SpecsMember member = new SpecsMember(name, affiliation, position, context, currentStatus);

			String visitingPeriod = (String) row.get(3);
			if (!visitingPeriod.isEmpty()) {
				member.setVisitingPeriod(visitingPeriod);
			}

			setField(5, member::setFirstJob, row);
			
//			if (row.size() > 5) {
//				String FirstJob = (String) row.get(5);
//				if (!FirstJob.isEmpty()) {
//					member.setFirstJob(FirstJob);
//				}
//			}

			setField(7, member::setStatus, row);
//			if (row.size() > 7) {
//				String Status = (String) row.get(7);
//				if (!Status.isEmpty()) {
//					member.setStatus(Status);
//				}
//			}
			setField(8, member::setORCID, row);
//			if (row.size() > 8) {
//				String ORCID = (String) row.get(8);
//				if (!ORCID.isEmpty()) {
//					member.setORCID(ORCID);
//				}
//			}
			
			setField(9, member::setDBLP, row);
//			if (row.size() > 9) {
//				String DBLP = (String) row.get(9);
//				if (!DBLP.isEmpty()) {
//					member.setDBLP(DBLP);
//				}
//			}
			
			setField(7, member::setStatus, row);
//			if (row.size() > 10) {
//				String ResearchGate = (String) row.get(10);
//				if (!ResearchGate.isEmpty()) {
//					member.setResearchGate(ResearchGate);
//				}
//			}
			setField(7, member::setStatus, row);
//			if (row.size() > 11) {
//				String SchoolarGoogle = (String) row.get(11);
//				if (!SchoolarGoogle.isEmpty()) {
//					member.setSchoolarGoogle(SchoolarGoogle);
//				}
//			}
//			setField(7, member::setStatus, row);
//			if (row.size() > 12) {
//				String Linkedin = (String) row.get(12);
//				if (!Linkedin.isEmpty()) {
//					member.setLinkedin(Linkedin);
//				}
////			}
			
			setField(7, member::setStatus, row);
//			if (row.size() > 13) {
//				String Twitter = (String) row.get(13);
//				if (!Twitter.isEmpty()) {
//					member.setTwitter(Twitter);
//				}
//			}
			
			setField(7, member::setStatus, row);
//			if (row.size() > 14) {
//				String WebPage = (String) row.get(14);
//				if (!WebPage.isEmpty()) {
//					member.setWebPage(WebPage);
//				}
//			}
			setField(7, member::setStatus, row);
//			if (row.size() > 15) {
//				String Email = (String) row.get(15);
//				if (!Email.isEmpty()) {
//					member.setEmail(Email);
//				}
//			}
			setField(7, member::setStatus, row);
//			if (row.size() > 16) {
//				String FirstJobMsc = (String) row.get(16);
//				if (!FirstJobMsc.isEmpty()) {
//					member.setFirstJobMsc(FirstJobMsc);
//				}
//			}
			setField(17, member::setPublicKey, row);
//			if (row.size() > 17) {
//				String PublicKey = (String) row.get(17);
//				if (!PublicKey.isEmpty()) {
//					member.setPublicKey(PublicKey);
//				}
//			}
			
			setField(18, member::setSupervisor, row);
//			if (row.size() > 18) {
//				String Supervisor = (String) row.get(18);
//				if (!Supervisor.isEmpty()) {
//					member.setSupervisor(Supervisor);
//				}
//
//			}

			members.add(member);

			
		}

		return infoFiles;
	}

}
