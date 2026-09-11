package br.com.ares.serviceorder.application.port.out;

import br.com.ares.serviceorder.application.port.in.ServiceOrderDocumentUseCase.ServiceOrderDocument;

public interface ServiceOrderPdfGenerator {

    byte[] generate(ServiceOrderDocument document);
}
