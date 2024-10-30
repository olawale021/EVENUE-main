package com.example.evenue.service;

import com.example.evenue.models.events.EventDao;
import com.example.evenue.models.events.EventModel;
import com.example.evenue.models.tickets.TicketDao;
import com.example.evenue.models.tickets.TicketTypeDao;
import com.example.evenue.models.users.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EventService {

    @Autowired
    private EventDao eventDao;

    @Autowired
    private TicketDao ticketDao;

    @Autowired
    private TicketTypeDao ticketTypeDao;

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    // Method to add an event
    public EventModel addEvent(EventModel event) {
        return eventDao.save(event);
    }

    public Page<EventModel> getAllEvents(Pageable pageable) {
        return eventDao.findAll(pageable);
    }

    public Optional<EventModel> getEventById(Long eventId) {
        if (eventId == null) {
            throw new IllegalArgumentException("Event ID must not be null");
        }
        return eventDao.findById(eventId);
    }


    // Implement filter logic with search functionality
    public Page<EventModel> filterEvents(Long categoryId, String search, String location, Pageable pageable) {
        if (categoryId != null && categoryId > 0) {
            return eventDao.findByCategoryAndSearch(categoryId, search, pageable);
        }
        return eventDao.findBySearchAndLocation(search, location, pageable);
    }


    // Method to find an event by name
    public Optional<EventModel> findByEventName(String eventName) {
        return eventDao.findByEventName(eventName); // Use Optional to avoid returning null
    }

    // Fetch events by tickets the user has
    public List<EventModel> getEventsByUser(UserModel user) {
        return ticketDao.findEventsByUser(user);
    }

    public List<EventModel> searchEvents(String searchQuery, String location) {
        return eventDao.findBySearchAndLocation(searchQuery, location, Pageable.unpaged()).getContent();
    }

    // Method to get all events for dropdown
    public List<EventModel> getAllEventsForDropdown() {
        return eventDao.findAllEvents();
    }

    public Page<EventModel> getFilteredEvents(List<Long> categories, String dateFilter, String priceFilter, String searchQuery, String location, Pageable pageable) {
        // Check if the categories list is empty and set it to null if it is
        if (categories != null && categories.isEmpty()) {
            categories = null;
        }

        LocalDate startDate = null;
        LocalDate endDate = null;

        // Date filter logic
        if ("today".equals(dateFilter)) {
            startDate = LocalDate.now();
            endDate = LocalDate.now();
        } else if ("this-week".equals(dateFilter)) {
            startDate = LocalDate.now();
            endDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        } else if ("this-month".equals(dateFilter)) {
            startDate = LocalDate.now();
            endDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        } else if ("within-2-weeks".equals(dateFilter)) {
            startDate = LocalDate.now();
            endDate = LocalDate.now().plusWeeks(2);
        } else if ("within-1-month".equals(dateFilter)) {
            startDate = LocalDate.now();
            endDate = LocalDate.now().plusMonths(1);
        }

        // Price filter logic
        Double minPrice = null;
        Double maxPrice = null;
        if ("free".equals(priceFilter)) {
            minPrice = 0.0;
            maxPrice = 0.0;
        } else if ("under-30".equals(priceFilter)) {
            maxPrice = 30.0;
        } else if ("between-30-and-100".equals(priceFilter)) {
            minPrice = 30.0;
            maxPrice = 100.0;
        } else if ("over-100".equals(priceFilter)) {
            minPrice = 100.0;
        }

        // Log the calculated filter values
//        logger.info("Categories: {}", categories);
//        logger.info("Date range: {} to {}", startDate, endDate);
//        logger.info("Price range: {} to {}", minPrice, maxPrice);
//        logger.info("Search Query: {}", searchQuery); // Ensure search query is logged
//        logger.info("Location: {}", location); // Log location

        // Call DAO method with all parameters
        Page<EventModel> events = eventDao.findByFilters(categories, searchQuery, startDate, endDate, minPrice, maxPrice, location, pageable);

//        logger.info("Number of events returned: {}", events.getTotalElements());

        return events;
    }

}
