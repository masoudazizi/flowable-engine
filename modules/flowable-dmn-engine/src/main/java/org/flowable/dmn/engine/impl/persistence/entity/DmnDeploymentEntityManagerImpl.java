/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flowable.dmn.engine.impl.persistence.entity;

import java.util.List;
import java.util.Map;

import org.flowable.common.engine.api.repository.EngineResource;
import org.flowable.common.engine.impl.persistence.entity.AbstractEngineEntityManager;
import org.flowable.dmn.api.DmnDecisionTable;
import org.flowable.dmn.api.DmnDeployment;
import org.flowable.dmn.engine.DmnEngineConfiguration;
import org.flowable.dmn.engine.impl.DmnDeploymentQueryImpl;
import org.flowable.dmn.engine.impl.persistence.entity.data.DmnDeploymentDataManager;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class DmnDeploymentEntityManagerImpl
    extends AbstractEngineEntityManager<DmnEngineConfiguration, DmnDeploymentEntity, DmnDeploymentDataManager>
    implements DmnDeploymentEntityManager {

    public DmnDeploymentEntityManagerImpl(DmnEngineConfiguration dmnEngineConfiguration, DmnDeploymentDataManager deploymentDataManager) {
        super(dmnEngineConfiguration, deploymentDataManager);
    }

    @Override
    public void insert(DmnDeploymentEntity deployment) {
        super.insert(deployment, true);

        for (EngineResource resource : deployment.getResources().values()) {
            resource.setDeploymentId(deployment.getId());
            getResourceEntityManager().insert((DmnResourceEntity) resource);
        }
    }

    @Override
    public void deleteDeployment(String deploymentId) {
        getHistoricDecisionExecutionEntityManager().deleteHistoricDecisionExecutionsByDeploymentId(deploymentId);
        getDecisionTableEntityManager().deleteDecisionTablesByDeploymentId(deploymentId);
        getResourceEntityManager().deleteResourcesByDeploymentId(deploymentId);
        delete(findById(deploymentId));
    }

    protected DecisionTableEntity findLatestDecisionTable(DmnDecisionTable decisionTable) {
        DecisionTableEntity latestDecisionTable = null;
        if (decisionTable.getTenantId() != null && !DmnEngineConfiguration.NO_TENANT_ID.equals(decisionTable.getTenantId())) {
            latestDecisionTable = getDecisionTableEntityManager()
                    .findLatestDecisionTableByKeyAndTenantId(decisionTable.getKey(), decisionTable.getTenantId());
        } else {
            latestDecisionTable = getDecisionTableEntityManager().findLatestDecisionTableByKey(decisionTable.getKey());
        }
        return latestDecisionTable;
    }

    @Override
    public long findDeploymentCountByQueryCriteria(DmnDeploymentQueryImpl deploymentQuery) {
        return dataManager.findDeploymentCountByQueryCriteria(deploymentQuery);
    }

    @Override
    public List<DmnDeployment> findDeploymentsByQueryCriteria(DmnDeploymentQueryImpl deploymentQuery) {
        return dataManager.findDeploymentsByQueryCriteria(deploymentQuery);
    }

    @Override
    public List<String> getDeploymentResourceNames(String deploymentId) {
        return dataManager.getDeploymentResourceNames(deploymentId);
    }

    @Override
    public List<DmnDeployment> findDeploymentsByNativeQuery(Map<String, Object> parameterMap) {
        return dataManager.findDeploymentsByNativeQuery(parameterMap);
    }

    @Override
    public long findDeploymentCountByNativeQuery(Map<String, Object> parameterMap) {
        return dataManager.findDeploymentCountByNativeQuery(parameterMap);
    }

    protected DmnResourceEntityManager getResourceEntityManager() {
        return engineConfiguration.getResourceEntityManager();
    }

    protected HistoricDecisionExecutionEntityManager getHistoricDecisionExecutionEntityManager() {
        return engineConfiguration.getHistoricDecisionExecutionEntityManager();
    }

    protected DecisionTableEntityManager getDecisionTableEntityManager() {
        return engineConfiguration.getDecisionTableEntityManager();
    }

}
