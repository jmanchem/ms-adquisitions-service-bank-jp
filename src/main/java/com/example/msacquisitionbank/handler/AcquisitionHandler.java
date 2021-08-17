package com.example.msacquisitionbank.handler;

import com.example.msacquisitionbank.models.dto.AverageBalanceDTO;
import com.example.msacquisitionbank.models.entities.Acquisition;
import com.example.msacquisitionbank.models.entities.Bill;
import com.example.msacquisitionbank.models.entities.Customer;
import com.example.msacquisitionbank.models.entities.Product;
import com.example.msacquisitionbank.services.BillService;
import com.example.msacquisitionbank.services.CustomerService;
import com.example.msacquisitionbank.services.IAcquisitionService;
import com.example.msacquisitionbank.services.ProductService;
import com.example.msacquisitionbank.utils.AccountNumberGenerator;
import com.example.msacquisitionbank.utils.CreditCardNumberGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;

@Component
@Slf4j(topic = "ACQUISITION_HANDLER")
public class AcquisitionHandler {

    private final IAcquisitionService acquisitionService;
    private final BillService billService;
    private final ProductService productService;
    private final CustomerService customerService;
    @Autowired
    public AcquisitionHandler(IAcquisitionService acquisitionService, BillService billService, ProductService productService, CustomerService customerService) {
        this.acquisitionService = acquisitionService;
        this.billService = billService;
        this.productService = productService;
        this.customerService = customerService;
    }

    public Mono<ServerResponse> findAll(ServerRequest request){
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(acquisitionService.findAll(), Acquisition.class);
    }

    public Mono<ServerResponse> findById(ServerRequest request){
        String id = request.pathVariable("id");
        return acquisitionService.findById(id).flatMap(acquisition -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(acquisition))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> findByCardNumber(ServerRequest request){
        String cardNumber = request.pathVariable("cardNumber");
        return acquisitionService.findByCardNumber(cardNumber).flatMap(acquisition -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(acquisition))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> findByProductName(ServerRequest request){
        String productName = request.pathVariable("productName");
        return productService.findByProductName(productName).flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p))
                .switchIfEmpty(Mono.error(new RuntimeException("THE PRODUCT DOES NOT EXIST")));
    }

    public Mono<ServerResponse> findByProductId(ServerRequest request){
        String productId = request.pathVariable("productId");
        return productService.findByProductId(productId).flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p))
                .switchIfEmpty(Mono.error(new RuntimeException("THE PRODUCT DOES NOT EXIST")));
    }

    public Mono<ServerResponse> findByIdentityNumber(ServerRequest request){
        String identityNumber = request.pathVariable("identityNumber");
        return customerService.findByIdentityNumber(identityNumber).flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p))
                .switchIfEmpty(Mono.error(new RuntimeException("THE PRODUCT DOES NOT EXIST")));
    }
    public Mono<ServerResponse> findAllByCustomer(ServerRequest request){
        String identityNumber = request.pathVariable("identityNumber");
        AverageBalanceDTO averageBalanceDTO = new AverageBalanceDTO();
        return customerService.findByIdentityNumber(identityNumber).flatMap(customer -> {
                    List<Customer> customers = new ArrayList<>();
                    customers.add(customer);
            return acquisitionService.findAllByCustomerHolder(customers).collectList().flatMap(acquisitions -> {
                //Mono<Bill> billMono = billService.findByCardNumber(acquisitions)
                averageBalanceDTO.setAcquisitions(acquisitions);
                int i = 0;
                double average = 0.0;
                //List<Bill> bills = new ArrayList<>();
                for (Acquisition acquisition : acquisitions){
                    average += acquisition.getBill().getBalance();
                    i++;
                }
                averageBalanceDTO.setAverage(average / i);
                return Mono.just(averageBalanceDTO);
            }).flatMap(p -> ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(p));
        });
    }

    public Mono<ServerResponse> createAcquisitionTest2(ServerRequest request){
        Mono<Acquisition> acquisition = request.bodyToMono(Acquisition.class);
        Acquisition acquisitionInit = new Acquisition();
        return acquisition.flatMap(acquisition1 -> productService.findByProductName(acquisition1.getProduct().getProductName())
                        .flatMap(product -> {
                            acquisitionInit.setProduct(product);
                            return Mono.just(acquisition1);
                        }).flatMap(acquisition2 -> Flux.fromIterable(acquisition2.getCustomerHolder())
                        .flatMap(customer -> customerService.findByIdentityNumber(customer.getCustomerIdentityNumber()))
                        .collectList()).flatMap(customers -> {
                            CreditCardNumberGenerator creditCardNumberGenerator = new CreditCardNumberGenerator();
                            acquisitionInit.setCustomerHolder(customers);
                            acquisitionInit.setInitial(acquisition1.getInitial());
                            acquisitionInit.setCardNumber(creditCardNumberGenerator.generate("4551", 17));
                            acquisitionInit.setCustomerAuthorizedSigner(new ArrayList<>());
                            return Mono.just(acquisitionInit);
                        })
                        .flatMap(acquisition3 -> {
                            long quantityHolder = acquisition3.getCustomerHolder().size();
                            long quantityEnterpriseHolder = acquisition3.getCustomerHolder()
                                    .stream().filter(ce -> ce.getCustomerType().equals("ENTERPRISE")).count();
                            long quantityPersonalHolder = acquisition3.getCustomerHolder()
                                    .stream().filter(ce -> ce.getCustomerType().equals("PERSONAL")).count();
                            boolean isEnterprise=false;
                            boolean isPersonal=false;
                            log.info("QUANTITY_2{}", quantityHolder>1);
                            log.info("QUANTITY_0 {}", quantityHolder==0);
                            log.info("QUANTITY_1 {}", quantityHolder==1);
                            //StringBuilder message = new StringBuilder();
                            isEnterprise = quantityEnterpriseHolder==quantityHolder&&quantityPersonalHolder==0;
                            isPersonal = quantityPersonalHolder==quantityHolder&&quantityEnterpriseHolder==0;
                            log.info("CLIENT_TYPE {}", isEnterprise);
                            log.info("CLIENT_TYPE {}", isPersonal);
                            if (isEnterprise){
                                if (acquisition3.getProduct().getProductName().equals("PLAZO FIJO")
                                || acquisition3.getProduct().getProductName().equals("AHORRO")){
                                    return Mono.error(new RuntimeException(String.format("The business customer cannot have account product of type : %s", acquisition3.getProduct().getProductName())));
                                }
                            }else{
                                return acquisitionService
                                        .findAll()
                                        .collectList()
                                        .flatMap(acquisitionsPersonal -> {
                                            log.info("QUANTITY_3{}", acquisition3.getCustomerHolder().size());
                                            int i = 0;
                                            for (Acquisition acquisition4 : acquisitionsPersonal){
                                                for (Customer customer : acquisition4.getCustomerHolder()){
                                                    for (Customer customer1 : acquisition3.getCustomerHolder()){
                                                        log.info("CLIENT_TYPE : {}", customer1.getCustomerType());
                                                        log.info("CLIENT_TYPE : {}", Objects.equals(customer1.getCustomerType(), "ENTERPRISE"));
                                                        if (customer.getCustomerIdentityNumber().equals(customer1.getCustomerIdentityNumber())
                                                                && acquisition4.getProduct().getProductName().equals(acquisition3.getProduct().getProductName())
                                                        ) {
                                                            i++;
                                                        }
                                                    }
                                                }
                                            }
                                            if (i > 0){
                                                return Mono.empty();
                                            }
                                            return Mono.just(acquisition3);
                                        }).switchIfEmpty(Mono.error(new RuntimeException(String.format("The client type personal has the %s account product", acquisition3.getProduct().getProductName()))));
                            }
                            return Mono.just(acquisition3);
                        }).flatMap(acquisitionBill -> {
                            if (acquisitionBill.getInitial() < 0){
                                return Mono.empty();
                            }
                            Bill bill = new Bill();
                            AccountNumberGenerator accountNumberGenerator = new AccountNumberGenerator();
                            bill.setAccountNumber(accountNumberGenerator.generate(15));
                            bill.setAcquisition(acquisitionBill);
                            bill.setBalance(acquisitionBill.getInitial());
                            return billService.createBill(bill);
                        }).switchIfEmpty(Mono.error(new RuntimeException("the initial amount must be greater than zero")))
                        .flatMap(bill -> {
                            acquisitionInit.setBill(bill);
                            return acquisitionService.create(acquisitionInit);
                        }))
                .flatMap(response -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response));
    }

    public Mono<ServerResponse> updateAcquisition(ServerRequest request){
        Mono<Acquisition> acquisition = request.bodyToMono(Acquisition.class);
        return acquisition.flatMap(acquisitionEdit -> acquisitionService.findByCardNumber(acquisitionEdit.getCardNumber()).flatMap(currentAcquisition -> {
            currentAcquisition.setProduct(acquisitionEdit.getProduct());
            return acquisitionService.update(currentAcquisition);
        })).flatMap(acquisitionResponse -> ServerResponse.created(URI.create("/api/acquisition/".concat(acquisitionResponse.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(acquisitionResponse));
    }
}
