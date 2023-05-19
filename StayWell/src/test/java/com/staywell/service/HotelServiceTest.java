package com.staywell.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.staywell.dto.HotelDTO;
import com.staywell.dto.UpdateHotelDetailsDTO;
import com.staywell.enums.HotelType;
import com.staywell.model.Address;
import com.staywell.model.Feedback;
import com.staywell.model.Hotel;
import com.staywell.model.Reservation;
import com.staywell.model.Room;
import com.staywell.repository.CustomerDao;
import com.staywell.repository.HotelDao;
import com.staywell.repository.ReservationDao;

@SpringBootTest(classes = HotelServiceTest.class) /*It is for configuration of application context*/
@ExtendWith(MockitoExtension.class) /**/
@TestMethodOrder(OrderAnnotation.class)
public class HotelServiceTest {
	
	@Mock
	private HotelDao hotelDao;
	
	@Mock
	private CustomerDao customerDao;
	
	@Mock
	private ReservationDao reservationDao;
	
	@Mock
	private SecurityContext securityContext;
	
	@Mock
	private Authentication authentication;
	
	@Mock
	private PasswordEncoder passwordEncoder;
	
	@InjectMocks
	private HotelServiceImpl hotelService;
	
	@Test
	@Order(1)
	public void testRegisterHotel() {
		
		
		Address address = new Address("Near Hospital", "Jaipur", "RJ", "123456", "India");
		List<String> amenities = new ArrayList<>();
		List<Room> rooms = new ArrayList<>();
		List<Reservation> reservations = new ArrayList<>();
		List<Feedback> feedbacks = new ArrayList<>();
		rooms.add(new Room(1001, 1, "AC", 2, BigDecimal.valueOf(5000.0), true, null, reservations));

		Hotel dummyHotel = new Hotel(Long.valueOf(1), "MyHotel", "myhotel@gmail.com", "9999999999", "9000000000", "1234", address, "HOTEL",
				HotelType.valueOf("Hotel"), amenities, rooms, reservations, feedbacks);
		
		
		
		when(customerDao.findByEmail(anyString())).thenReturn(Optional.empty());
		doReturn(Optional.empty()).when(hotelDao).findByHotelEmail(anyString());
		
		when(hotelDao.save(any())).thenReturn(dummyHotel);
		
		HotelDTO hotelDTO = new HotelDTO();
		hotelDTO.setEmail("any@gmail.com");
		
		Hotel hotel = hotelService.registerHotel(hotelDTO);
		assertEquals("myhotel@gmail.com", hotel.getHotelEmail());
		
	}

	@Test
	@Order(2)
	public void testGetHotelById() {
		Address address = new Address("Near Hospital", "Jaipur", "RJ", "123456", "India");
		List<String> amenities = new ArrayList<>();
		List<Room> rooms = new ArrayList<>();
		List<Reservation> reservations = new ArrayList<>();
		List<Feedback> feedbacks = new ArrayList<>();
		rooms.add(new Room(1001, 1, "AC", 2, BigDecimal.valueOf(5000.0), true, null, reservations));

		Hotel dummyHotel = new Hotel(Long.valueOf(1), "MyHotel", "myhotel@gmail.com", "9999999999", "9000000000", "1234", address, "HOTEL",
				HotelType.valueOf("Hotel"), amenities, rooms, reservations, feedbacks);
		
		when(hotelDao.findById(anyLong())).thenReturn(Optional.of(dummyHotel));
		
		Hotel hotel = hotelService.getHotelById(1L);
		
		assertEquals(1L, hotel.getHotelId());
		assertEquals("myhotel@gmail.com", hotel.getHotelEmail());
		
	}
	
	@Test
	@Order(3)
	public void testUpdateEmail() {
		
		Address address = new Address("Near Hospital", "Jaipur", "RJ", "123456", "India");
		List<String> amenities = new ArrayList<>();
		List<Room> rooms = new ArrayList<>();
		List<Reservation> reservations = new ArrayList<>();
		List<Feedback> feedbacks = new ArrayList<>();
		rooms.add(new Room(1001, 1, "AC", 2, BigDecimal.valueOf(5000.0), true, null, reservations));

		Hotel dummyHotel = new Hotel(Long.valueOf(1), "MyHotel", "myhotel@gmail.com", "9999999999", "9000000000", "1234", address, "HOTEL",
				HotelType.valueOf("Hotel"), amenities, rooms, reservations, feedbacks);
		
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn("abc@gmailcom");

		when(hotelDao.findByHotelEmail(anyString())).thenReturn(Optional.of(dummyHotel));
		
		UpdateHotelDetailsDTO updateDTO = new UpdateHotelDetailsDTO();
		updateDTO.setPassword("1234");
		updateDTO.setField("hotel@gmail.com");
		
		
		
	}
	
}
