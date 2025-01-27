/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.account.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.collectionspace.services.account.AccountTenant;
import org.collectionspace.services.account.AccountsCommon;
import org.collectionspace.services.account.AccountsCommonList;
import org.collectionspace.services.account.AccountListItem;
import org.collectionspace.services.account.Status;

import org.collectionspace.services.client.AccountClient;
import org.collectionspace.services.common.storage.jpa.JpaDocumentHandler;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.JaxbUtils;
import org.collectionspace.services.common.security.SecurityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 
 */
public class AccountDocumentHandler
        extends JpaDocumentHandler<AccountsCommon, AccountsCommonList, AccountsCommon, List> {

    private final Logger logger = LoggerFactory.getLogger(AccountDocumentHandler.class);
    private AccountsCommon account;
    private AccountsCommonList accountList;

    @Override
    public void handleCreate(DocumentWrapper<AccountsCommon> wrapDoc) throws Exception {
        String id = UUID.randomUUID().toString();
        AccountsCommon account = wrapDoc.getWrappedObject();
        account.setCsid(id);
        setTenant(account);
        account.setStatus(Status.ACTIVE);
        // We do not allow creation of locked accounts through the services.
        account.setMetadataProtection(null);
        account.setRolesProtection(null);
    }

    @Override
    public void handleUpdate(DocumentWrapper<AccountsCommon> wrapDoc) throws Exception {
        AccountsCommon accountFound = wrapDoc.getWrappedObject();
        AccountsCommon accountReceived = getCommonPart();
        // If marked as metadata immutable, do not do update
        if(!AccountClient.IMMUTABLE.equals(accountFound.getMetadataProtection())) {
        	merge(accountReceived, accountFound);
        }
    }

    /**
     * merge manually merges the from account to the to account
     * -this method is created due to inefficiency of JPA EM merge
     * @param from
     * @param to
     * @return merged account
     */
    private AccountsCommon merge(AccountsCommon from, AccountsCommon to) {
        Date now = new Date();
        to.setUpdatedAtItem(now);
        if (from.getEmail() != null) {
            to.setEmail(from.getEmail());
        }
        if (from.getPhone() != null) {
            to.setPhone(from.getPhone());
        }
        if (from.getMobile() != null) {
            to.setMobile(from.getMobile());
        }
        if (from.getScreenName() != null) {
            to.setScreenName(from.getScreenName());
        }
        if (from.getStatus() != null) {
            to.setStatus(from.getStatus());
        }
        if (from.getPersonRefName() != null) {
            to.setPersonRefName(from.getPersonRefName());
        }
        // Note that we do not allow update of locks
        //fixme update for tenant association

        if (logger.isDebugEnabled()) {
            logger.debug("merged account="
                    + JaxbUtils.toString(to, AccountsCommon.class));
        }
        return to;
    }

    @Override
    public void completeUpdate(DocumentWrapper<AccountsCommon> wrapDoc) throws Exception {
        AccountsCommon upAcc = wrapDoc.getWrappedObject();
        getServiceContext().setOutput(upAcc);
        sanitize(upAcc);
    }

    @Override
    public void handleGet(DocumentWrapper<AccountsCommon> wrapDoc) throws Exception {
        setCommonPart(extractCommonPart(wrapDoc));
        sanitize(getCommonPart());
        getServiceContext().setOutput(account);
    }

    @Override
    public void handleGetAll(DocumentWrapper<List> wrapDoc) throws Exception {
        AccountsCommonList accList = extractCommonPartList(wrapDoc);
        setCommonPartList(accList);
        getServiceContext().setOutput(getCommonPartList());
    }

    @Override
    public AccountsCommon extractCommonPart(
            DocumentWrapper<AccountsCommon> wrapDoc)
            throws Exception {
        return wrapDoc.getWrappedObject();
    }

    @Override
    public void fillCommonPart(AccountsCommon obj, DocumentWrapper<AccountsCommon> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException("operation not relevant for AccountDocumentHandler");
    }

    @Override
    public AccountsCommonList extractCommonPartList(
            DocumentWrapper<List> wrapDoc)
            throws Exception {

    	AccountsCommonList accList = this.extractPagingInfo(new AccountsCommonList(), wrapDoc);
//        AccountsCommonList accList = new AccountsCommonList();
        List<AccountListItem> list = accList.getAccountListItem();

        for (Object obj : wrapDoc.getWrappedObject()) {
            AccountsCommon account = (AccountsCommon) obj;
            AccountListItem accListItem = new AccountListItem();
            accListItem.setScreenName(account.getScreenName());
            accListItem.setUserid(account.getUserId());
            accListItem.setTenantid(account.getTenants().get(0).getTenantId()); // pick the default/first tenant
            accListItem.setTenants(account.getTenants());
            accListItem.setEmail(account.getEmail());
            accListItem.setStatus(account.getStatus());
            String id = account.getCsid();
            accListItem.setUri(getServiceContextPath() + id);
            accListItem.setCsid(id);
            list.add(accListItem);
        }
        return accList;
    }

    @Override
    public AccountsCommon getCommonPart() {
        return account;
    }

    @Override
    public void setCommonPart(AccountsCommon account) {
        this.account = account;
    }

    @Override
    public AccountsCommonList getCommonPartList() {
        return accountList;
    }

    @Override
    public void setCommonPartList(AccountsCommonList accountList) {
        this.accountList = accountList;
    }

    @Override
    public String getQProperty(
            String prop) {
        return null;
    }

    @Override
    public DocumentFilter createDocumentFilter() {
        DocumentFilter filter = new AccountJpaFilter(this.getServiceContext());
        return filter;
    }

    private void setTenant(AccountsCommon account) {
        //set tenant only if not available from input
        ServiceContext ctx = getServiceContext();
        if (account.getTenants() == null || account.getTenants().size() == 0) {
            if (ctx.getTenantId() != null) {
                AccountTenant at = new AccountTenant();
                at.setTenantId(ctx.getTenantId());
                List<AccountTenant> atList = new ArrayList<AccountTenant>();
                atList.add(at);
                account.setTenants(atList);
            }
        }
    }

    /**
     * sanitize removes data not needed to be sent to the consumer
     * @param account
     */
    private void sanitize(AccountsCommon account) {
        account.setPassword(null);
        if (!SecurityUtils.isCSpaceAdmin()) {
            account.setTenants(new ArrayList<AccountTenant>(0));
        }
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#initializeDocumentFilter(org.collectionspace.services.common.context.ServiceContext)
     */
    public void initializeDocumentFilter(ServiceContext ctx) {
        // set a default document filter in this method
    }
}
