package org.demoiselle.signer.policy.engine.asn1.icpb.v2;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
import org.demoiselle.signer.policy.engine.asn1.GeneralizedTime;
import org.demoiselle.signer.policy.engine.asn1.etsi.SignaturePolicy;
import org.demoiselle.signer.policy.engine.exception.PolicyException;
import org.demoiselle.signer.policy.engine.factory.PolicyFactory;
import org.demoiselle.signer.policy.engine.repository.LPARepository;
import org.demoiselle.signer.policy.engine.util.MessagesBundle;

public class PolicyValidator {
	
	private static MessagesBundle policyMessagesBundle = new MessagesBundle("messages_policy");
	private final static Logger LOGGER = Logger.getLogger(PolicyValidator.class.getName());
	
	private SignaturePolicy sp;
	private String policyName;
	private LPA listOfPolicies;
	
	public PolicyValidator(SignaturePolicy sp, String policyName) {
		super();
		this.sp = sp;
		this.policyName = policyName;
	}

	
	public boolean validate(){
		try{
			boolean valid = true;
			
			Date dateNotBefore = this.sp.getSignPolicyInfo().getSignatureValidationPolicy().getSigningPeriod()
					.getNotBefore().getDate();
			Date dateNotAfter = this.sp.getSignPolicyInfo().getSignatureValidationPolicy().getSigningPeriod()
					.getNotAfter().getDate();
			
			Date actualDate = new GregorianCalendar().getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss");
					
			if (actualDate.before(dateNotBefore) || actualDate.after(dateNotAfter)) {				
				throw new PolicyException(policyMessagesBundle.getString("error.policy.valid.period",sdf.format(dateNotBefore), sdf.format(dateNotBefore)));
			}
			PolicyFactory factory = PolicyFactory.getInstance();
			
			LPA tempListOfPolicies= null;
			
			if (policyName.contains("CADES")){
				tempListOfPolicies = factory.loadLPACAdES();
				listOfPolicies = tempListOfPolicies;
				Date nextUpdate = tempListOfPolicies.getNextUpdate().getDate();
				if (actualDate.after(nextUpdate)){
					LOGGER.warn(policyMessagesBundle.getString("error.policy.not.updated",sdf.format(nextUpdate)));
					LOGGER.info(policyMessagesBundle.getString("info.lpa.load.local"));
					tempListOfPolicies = factory.loadLPACAdESLocal();
					if (tempListOfPolicies != null){
						nextUpdate = tempListOfPolicies.getNextUpdate().getDate();
						if (actualDate.after(nextUpdate)){
							LOGGER.warn(policyMessagesBundle.getString("error.policy.local.not.updated",LPARepository.FULL_PATH_FOLDER_SIGNER.toString()+"LPA_CAdES.der",sdf.format(nextUpdate)));
							tempListOfPolicies = factory.loadLPACAdESUrl();
							if (tempListOfPolicies != null)	{
								nextUpdate = tempListOfPolicies.getNextUpdate().getDate();
								if (actualDate.after(nextUpdate)){
									LOGGER.warn(policyMessagesBundle.getString("error.policy.not.updated",sdf.format(nextUpdate)));
								}else{
									listOfPolicies = tempListOfPolicies;
								}
							}
						}else{
							listOfPolicies = tempListOfPolicies;
						}
					}else{
						tempListOfPolicies = factory.loadLPACAdESUrl();
						if (tempListOfPolicies != null)	{
							nextUpdate = tempListOfPolicies.getNextUpdate().getDate();
							if (actualDate.after(nextUpdate)){
								LOGGER.warn(policyMessagesBundle.getString("error.policy.not.updated",sdf.format(nextUpdate)));
							}else{
								listOfPolicies = tempListOfPolicies;
							}
						}else{
							LOGGER.warn(policyMessagesBundle.getString("error.lpa.not.found"));
						}							
					}
				}
				
				
				for (PolicyInfo policyInfo : listOfPolicies.getPolicyInfos()) {
					if (policyInfo.getPolicyOID().getValue().contentEquals(sp.getSignPolicyInfo().getSignPolicyIdentifier().getValue())){
						GeneralizedTime revocationDate = policyInfo.getRevocationDate();
						if (revocationDate != null){
							throw new PolicyException(policyMessagesBundle.getString("error.policy.revocated",sdf.format(revocationDate.getDate())));
						}						
					}
				}
			}else {
				if (policyName.contains("PADES")){
					tempListOfPolicies = factory.loadLPAPAdES();
					listOfPolicies = tempListOfPolicies;
					Date nextUpdate = tempListOfPolicies.getNextUpdate().getDate();
					if (actualDate.after(nextUpdate)){
						LOGGER.warn(policyMessagesBundle.getString("error.policy.not.updated",sdf.format(nextUpdate)));
						LOGGER.info(policyMessagesBundle.getString("info.lpa.load.local"));
						tempListOfPolicies = factory.loadLPAPAdESLocal();
						if (tempListOfPolicies != null){
							nextUpdate = tempListOfPolicies.getNextUpdate().getDate();
							if (actualDate.after(nextUpdate)){
								LOGGER.warn(policyMessagesBundle.getString("error.policy.local.not.updated",LPARepository.FULL_PATH_FOLDER_SIGNER.toString()+"LPA_PAdES.der",sdf.format(nextUpdate)));
								tempListOfPolicies = factory.loadLPAPAdESUrl();
								if (tempListOfPolicies != null)	{
									nextUpdate = tempListOfPolicies.getNextUpdate().getDate();
									if (actualDate.after(nextUpdate)){
										LOGGER.warn(policyMessagesBundle.getString("error.policy.not.updated",sdf.format(nextUpdate)));
									}else{
										listOfPolicies = tempListOfPolicies;
									}
								}
							}else{
								listOfPolicies = tempListOfPolicies;
							}
						}else{
							tempListOfPolicies = factory.loadLPAPAdESUrl();
							if (tempListOfPolicies != null)	{
								nextUpdate = tempListOfPolicies.getNextUpdate().getDate();
								if (actualDate.after(nextUpdate)){
									LOGGER.warn(policyMessagesBundle.getString("error.policy.not.updated",sdf.format(nextUpdate)));
								}else{
									listOfPolicies = tempListOfPolicies;
								}
							}else{
								LOGGER.warn(policyMessagesBundle.getString("error.lpa.not.found"));
							}							
						}
					}
					for (PolicyInfo policyInfo : listOfPolicies.getPolicyInfos()) {
						if (policyInfo.getPolicyOID().getValue().contentEquals(sp.getSignPolicyInfo().getSignPolicyIdentifier().getValue())){
							GeneralizedTime revocationDate = policyInfo.getRevocationDate();
							if (revocationDate != null){
								throw new PolicyException(policyMessagesBundle.getString("error.policy.revocated",sdf.format(revocationDate.getDate())));
							}						
						}
					}
				} else{
					if (policyName.contains("XADES")){
						// TODO verificar como é procesado em XML
						listOfPolicies = factory.loadLPAXAdES();
					}else{
						throw new PolicyException(policyMessagesBundle.getString("error.policy.not.recognized", policyName));
					}
				}
			}			
			
			return valid;			
		}catch(Exception ex){
			throw new PolicyException(ex.getMessage(), ex);			
		}		
	}	
}
