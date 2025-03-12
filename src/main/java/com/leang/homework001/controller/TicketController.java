package com.leang.homework001.controller;

import com.leang.homework001.model.TicketStatus;
import com.leang.homework001.model.entity.Ticket;
import com.leang.homework001.model.request.TicketRequest;
import com.leang.homework001.model.request.UpdatePaymentStatusRequest;
import com.leang.homework001.model.response.APIResponse;
import com.leang.homework001.model.response.PageResponseList;
import com.leang.homework001.model.response.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {
    private final static List<Ticket> TICKETS = new ArrayList<>();
    private final static AtomicLong ATOMIC_LONG = new AtomicLong(4L);

    public TicketController() {
        TICKETS.add(new Ticket(1L, "John Doe", LocalDate.of(2024, 12, 20), "Station B", "Station B", 100.0, true, TicketStatus.COMPLETED, "A1"));
        TICKETS.add(new Ticket(2L, "John", LocalDate.of(2024, 12, 20), "Station C", "Station B", 110.0, false, TicketStatus.CANCELED, "A2"));
        TICKETS.add(new Ticket(3L, "Doe", LocalDate.of(2024, 12, 20), "Station A", "Station C", 100.0, false, TicketStatus.BOOKED, "A3"));
    }

    @Operation(summary = "Get all tickets")
    @GetMapping
    public ResponseEntity<APIResponse<PageResponseList<Ticket>>> getAllTickets(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        int totalPage = (int) Math.ceil((double) TICKETS.size() / size);
        List<Ticket> ticketList = TICKETS.stream().skip((long) (page - 1) * size).limit(size).toList();
        PaginationResponse pagination = PaginationResponse.builder().totalElement(TICKETS.size()).currentPage(page).pageSize(size).totalPage(totalPage).build();
        PageResponseList<Ticket> payload = PageResponseList.<Ticket>builder().items(ticketList).pagination(pagination).build();
        return ResponseEntity.ok(APIResponse.<PageResponseList<Ticket>>builder().success(true).message("All tickets retrieved successfully").status("OK").payload(payload).timeStamp(LocalDateTime.now()).build());
    }

    @Operation(summary = "Bulk update payment status for multiple tickets")
    @PutMapping
    public ResponseEntity<APIResponse<List<Ticket>>> updatePaymentStatusBulk(@RequestBody UpdatePaymentStatusRequest request) {
        List<Ticket> tickets = new ArrayList<>();
        for (int ticketID : request.getTickedIds()) {
            for (Ticket ticket : TICKETS) {
                if (ticketID == ticket.getId()) {
                    ticket.setPaymentStatus(request.getPaymentStatus());
                    tickets.add(ticket);
                }
            }
        }
        return ResponseEntity.ok(APIResponse.<List<Ticket>>builder().success(true).message("Payment status updated successfully.").status("OK").payload(tickets).timeStamp(LocalDateTime.now()).build());
    }

    @Operation(summary = "Create a new ticket")
    @PostMapping
    public ResponseEntity<APIResponse<Ticket>> saveTicket(@RequestBody TicketRequest request) {
        Ticket ticket = new Ticket(ATOMIC_LONG.getAndIncrement(), request.getPassengerName(), request.getTravelDate(), request.getSourceStation(), request.getDestinationStation(), request.getPrice(), request.getPaymentStatus(), request.getTicketStatus(), request.getSeatNumber());
        TICKETS.add(ticket);
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.<Ticket>builder().success(true).message("Ticket has been saved").status("OK").payload(ticket).timeStamp(LocalDateTime.now()).build());

    }

    @Operation(summary = "Get a ticket by ID")
    @GetMapping("{ticket-id}")
    public ResponseEntity<APIResponse<Ticket>> getTicketById(@PathVariable("ticket-id") Long ticketId) {
        for (Ticket ticket : TICKETS) {
            if (ticket.getId().equals(ticketId)) {
                return ResponseEntity.ok(APIResponse.<Ticket>builder().success(true).message("Ticket retrieved successfully.").status("OK").payload(ticket).timeStamp(LocalDateTime.now()).build());
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.<Ticket>builder().success(false).message("Ticket not found with ID: " + ticketId).status("NOT FOUND").timeStamp(LocalDateTime.now()).build());
    }

    @Operation(summary = "Updating an existing ticket by ID")
    @PutMapping("{ticket-id}")
    public ResponseEntity<?> updateTicketById(@PathVariable("ticket-id") Long ticketId, @RequestBody TicketRequest request, WebRequest webRequest) {
        ResponseEntity<?> problemDetail = unitPriceValidation(request, webRequest);
        if (problemDetail != null) return problemDetail;
        for (Ticket ticket : TICKETS) {
            if (ticket.getId().equals(ticketId)) {
                ticket.setPassengerName(request.getPassengerName());
                ticket.setTravelDate(request.getTravelDate());
                ticket.setSourceStation(request.getSourceStation());
                ticket.setDestinationStation(request.getDestinationStation());
                ticket.setPrice(request.getPrice());
                ticket.setPaymentStatus(request.getPaymentStatus());
                ticket.setTicketStatus(request.getTicketStatus());
                ticket.setSeatNumber(request.getSeatNumber());
                return ResponseEntity.ok(APIResponse.<Ticket>builder().success(true).message("Ticket updated successfully.").status("OK").payload(ticket).timeStamp(LocalDateTime.now()).build());
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.<Ticket>builder().success(false).message("No ticket found with ID: " + ticketId).status("NOT FOUND").timeStamp(LocalDateTime.now()).build());
    }

    private ResponseEntity<?> unitPriceValidation(@RequestBody TicketRequest request, WebRequest webRequest) {
        if (request.getPrice() <= 0) {
            ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
            problemDetail.setType(URI.create("about:blank"));
            problemDetail.setTitle("Bad Request");
            problemDetail.setStatus(400);
            problemDetail.setInstance(URI.create(webRequest.getDescription(false).replace("uri=", "")));
            problemDetail.setProperty("timestamp", LocalDateTime.now());
            problemDetail.setProperty("errors", Map.of("ticketRequest", "must be greater than 0"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
        }
        return null;
    }

    @Operation(summary = "Delete a ticket by ID")
    @DeleteMapping("{ticket-id}")
    public ResponseEntity<APIResponse> removeTicketById(@PathVariable("ticket-id") Long ticketId) {
        for (Ticket ticket : TICKETS) {
            if (ticket.getId().equals(ticketId)) {
                TICKETS.remove(ticket);
                return ResponseEntity.ok(APIResponse.<Ticket>builder().success(true).message("Ticket deleted successfully.").status("OK").timeStamp(LocalDateTime.now()).build());
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.<Ticket>builder().success(false).message("Ticket not found").status("NOT FOUND").timeStamp(LocalDateTime.now()).build());
    }

    @Operation(summary = "Bulk create tickets")
    @PostMapping("bulk")
    public ResponseEntity<?> removeTicketById(@RequestBody List<TicketRequest> tickets,WebRequest webRequest) {
        for (TicketRequest request : tickets) {
            ResponseEntity<?> problemDetail = unitPriceValidation(request, webRequest);
            if (problemDetail != null) return problemDetail;
        }
        List<Ticket> ticketList = new ArrayList<>();
        for (TicketRequest request : tickets) {
            Ticket ticket = new Ticket(ATOMIC_LONG.getAndIncrement(), request.getPassengerName(), request.getTravelDate(), request.getSourceStation(), request.getDestinationStation(), request.getPrice(), request.getPaymentStatus(), request.getTicketStatus(), request.getSeatNumber());
            TICKETS.add(ticket);
            ticketList.add(ticket);
        }
        return ResponseEntity.ok(APIResponse.<List<Ticket>>builder().success(true).message("Bulk tickets created successfully.").status("CREATED").payload(ticketList).timeStamp(LocalDateTime.now()).build());
    }

    @Operation(summary = "Search tickets by passenger name")
    @GetMapping("search")
    public ResponseEntity<APIResponse<List<Ticket>>> searchTicketByPassengerName(@RequestParam String name) {
        List<Ticket> tickets = new ArrayList<>();
        for (Ticket ticket : TICKETS) {
            if (ticket.getPassengerName().toLowerCase().contains(name.toLowerCase())) {
                tickets.add(ticket);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.
                <List<Ticket>>builder()
                .success(true)
                .message("Tickets searched successfully.")
                .status("OK").payload(tickets)
                .timeStamp(LocalDateTime.now()).build());
    }

    @Operation(summary = "Filter tickets by status and travel date")
    @GetMapping("filter")
    public ResponseEntity<APIResponse<List<Ticket>>> filterTicketByStatusAndTravelDate(@RequestParam TicketStatus ticketStatus, @RequestParam LocalDate travelDate) {
        List<Ticket> tickets = new ArrayList<>();
        for (Ticket ticket : TICKETS) {
            if (ticket.getTicketStatus().equals(ticketStatus) && ticket.getTravelDate().equals(travelDate)) {
                tickets.add(ticket);
            }
        }
        return ResponseEntity.ok().body(
                APIResponse.<List<Ticket>>builder()
                        .success(true)
                        .message("Tickets filtered successfully.")
                        .status("OK")
                        .payload(tickets)
                        .timeStamp(LocalDateTime.now()).build());
    }
}
