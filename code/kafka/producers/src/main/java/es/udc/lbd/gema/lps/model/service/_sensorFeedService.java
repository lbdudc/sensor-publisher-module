/*%@
  if (!feature.SV_D_DataInsertion || !feature.SV_D_DI_Producers) return [];
  return data.dataWarehouse.sensors
    .map(function(sen) {
      return {
          fileName: normalize(sen.id, true) + 'FeedService.java',
          context: sen
      };
    });
%*/
package es.udc.lbd.gema.lps.model.service;

public interface /*%= normalize(context.id, true) %*/FeedService {

  public void fetch/*%= normalize(context.id, true) %*/InfoRealTime() throws Exception;

}
