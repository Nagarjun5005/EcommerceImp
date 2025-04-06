# Pagination and Sorting in Spring Boot

This project demonstrates how to implement pagination and sorting in a Spring Boot application while using the 
DTO pattern for data encapsulation and transformation.

### Table of Contents
Overview
**_DTO Pattern
*     What is a DTO?
*     Why Use DTOs?
*     Input DTOs vs Output DTOs
*     Mapping DTOs to Entities
*     Model Mapper
Pagination in Spring Boot
*    Concept & Benefits
*    Implementing Pagination with Spring Data JPA
*    Controller Example
Request Parameters
Updating Response Structure
Methods Available in Page<T>
Setting Default Values
Implementing Sorting
References_**

### Overview
This documentation explains how to implement pagination and sorting in a Spring Boot application.
It also covers the use of the Data Transfer Object (DTO) pattern to separate internal domain models
from what is exposed through APIs. The primary goals are to improve performance when handling large data sets, 
ensure data encapsulation, and provide a flexible API response structure.


### DTO Pattern

##### What is a DTO?

A DTO (Data Transfer Object) is a simple Java object (POJO) that:
* Holds only data—no business logic.
* Typically contains fields, getters/setters, and maybe annotations for things like validation or JSON mapping.
* Is used to decouple internal models/entities from external clients or layers.

#####  Why use DTOs?

Encapsulation & Abstraction
Hide internal details (like entity relationships) from clients or APIs.

**Security**
Avoid exposing sensitive fields from your entities (e.g., passwords, internal IDs).

**Custom Structures**
You can shape the data exactly how the client needs it (e.g., combine multiple entity fields into one DTO).

**Validation**
DTOs can have annotations like @NotNull, @Size, etc., for validating incoming request data.

##### 📥 Input DTOs vs 📤 Output DTOs
✅ Input DTO
Used for receiving data from the client:

example :
`public class CreateUserRequest {
@NotBlank
private String username;
    @Email
    private String email;
    @NotBlank
    private String password;
}`

✅ Output DTO
Used to send data to the client:

example :
`public class UserResponse {
private String username;
private String email;
}`

##### 🛠 Mapping DTO <--> Entity
✅ Manual Mapping Example:

`public UserDTO convertToDto(User user) {
UserDTO dto = new UserDTO();
dto.setUsername(user.getUsername());
dto.setEmail(user.getEmail());
return dto;
}`

`public User convertToEntity(UserDTO dto) {
User user = new User();
user.setUsername(dto.getUsername());
user.setEmail(dto.getEmail());
return user;
}`

✅ Auto-Mapping Libraries
1)Model Mapper : Used in the project 
2)Map Struct   : Better performance when compared to model mapper


##### Model Mapper  

[Model Mapper](https://modelmapper.org/getting-started/)
<dependency>
    <groupId>org.modelmapper</groupId>
    <artifactId>modelmapper</artifactId>
    <version>3.1.0</version>
</dependency>

`ModelMapper modelMapper = new ModelMapper();
UserDTO dto = modelMapper.map(user, UserDTO.class);`


### Pagination
--- In Spring Boot,  is an annotation used to extract query parameters from the URL of an HTTP request.
It allows you to bind these parameters to method arguments in a controller, making it easier to 
handle user input sent via URLs.
--- 
Overview
The code demonstrates a controller method and a service method  to implement pagination for fetching categories.
It uses query parameters ( and ) provided via  to determine which page of data to retrieve and how many items should be
present in each page.

---
Pagination in Spring Boot
Pagination is a technique used to retrieve large sets of data in smaller, manageable chunks rather than fetching everything at once. 
This improves performance and reduces memory usage.

In the provided method, pagination is implemented using Spring Data JPA:

1) Pageable Interface:
``` Pageable = PageRequest.of(pageNumber, pageSize); ```
PageRequest.of(pageNumber, pageSize) creates a Pageable object that defines the page number 
and the number of records per page.

2) Fetching Paginated Data:
``` java Page<Category> categoryPage = categoryRepository.findAll(pageable); ```
This fetches a specific page of Category entities from the database.

3) Extracting Content:
``` java List<Category> categories = categoryPage.getContent();```
Retrieves the list of categories from the fetched page.

4) Handling Empty Data:
If there are no categories, an exception (APIException) is thrown.

5) Mapping Entities to DTOs:

The Category entities are converted into CategoryDTO using ModelMapper.

6) Returning the Response:

The converted list of categories is set in the CategoryResponse object, which is then returned.

### Request Param

Request parameters are used to send additional information in an HTTP request.
In this case, pageNumber and pageSize are passed as request parameters to control pagination.

Example API Call (Query Parameters in URL):

```GET /api/categories?pageNumber=0&pageSize=10 ```
pageNumber=0: Retrieves the first page (pagination is zero-based).
pageSize=10: Retrieves 10 records per page.

Spring Boot Controller Method (Using @RequestParam):

```java
@GetMapping("/categories")
public CategoryResponse getAllCategories(
@RequestParam(defaultValue = "0") Integer pageNumber,
@RequestParam(defaultValue = "10") Integer pageSize) {
return categoryService.getAllcategories(pageNumber, pageSize);
}

```
@RequestParam(defaultValue = "0"): If pageNumber is not provided, it defaults to 0.
@RequestParam(defaultValue = "10"): If pageSize is not provided, it defaults to 10.

The response will look like this:

{
"content": [
{
"id": 1,
"name": "Technology"
},
{
"id": 2,
"name": "Science"
}
]
}
This allows clients to request paginated category data dynamically by specifying pageNumber 
and pageSize.

### Updating Response Structure to Include Pagination Details

--- 
`@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
private List<CategoryDTO> content;
private Integer pageNumber;
private Integer pageSize;
private Integer totalPages;
private Boolean lastPage;
private Long totalItems;
}`

**Field Explanation**
 **Field	    Type             	Description**
1. content	List<CategoryDTO>	List of category data
2. pageNumber	Integer	Current page number
3. pageSize	Integer	Number of items per page
4. totalPages	Integer	Total number of pages available
5. totalItems	Long	Total number of categories in the database
6. lastPage	Boolean	Indicates if the current page is the last page


**Build CategoryResponse Object**
Populates the CategoryResponse object with the paginated details.
`CategoryResponse categoryResponse = new CategoryResponse();
categoryResponse.setContent(categoryList);
categoryResponse.setPageNumber(categoryPage.getNumber());
categoryResponse.setPageSize(categoryPage.getSize());
categoryResponse.setTotalPages(categoryPage.getTotalPages());
categoryResponse.setTotalItems(categoryPage.getTotalElements());
categoryResponse.setLastPage(categoryPage.isLast());`


### Methods Available in Page<T>

The Page<T> interface extends Slice<T>, so it includes both pagination and navigation methods.

##### 1️⃣ Pagination Methods

**Method	          Return Type       	Description**
getContent()	   List<T>	        Returns the content of the page as a list.
getTotalElements()	long	        Returns the total number of elements across all pages.
getTotalPages()	    int             Returns the total number of pages.
getSize()	        int             Returns the number of elements per page.
getNumber()	        int	            Returns the current page number (0-based index).
getNumberOfElements()	int	        Returns the number of elements on the current page.

##### *️⃣ Navigation Methods

**Method	   Return Type	  Description**
isFirst()	   boolean	      Returns true if this is the first page.
isLast()	   boolean	      Returns true if this is the last page.
hasNext()	   boolean	      Returns true if there is a next page.
hasPrevious()  boolean	      Returns true if there is a previous page.
nextPageable() Pageable       Returns a Pageable for the next page, if available.
previousPageable()	Pageable  Returns a Pageable for the previous page, if available.

##### **3️⃣ Sorting Methods**

**Method	Return Type	Description**
getSort()	Sort	Returns the sorting criteria used.
isSorted()	boolean	Returns true if the page is sorted.
isEmpty()	boolean	Returns true if there are no elements.

##### 4️⃣ Stream & Iterator Methods

Method	    Return Type	                         Description
stream()	Stream<T>	                     Returns a stream of elements on the current page.
iterator()	Iterator<T>                      Returns an iterator over the elements.
forEach(Consumer<? super T> action)	void	 Performs an action for each element.

##### 5️⃣ Conversion Methods

**Method	                                   Return Type	 Description**
map(Function<? super T, ? extends U> converter)	Page<U>	Converts the page content to another type.
🔹 Example Usage of Page<T> Methods
`Page<Category> categoryPage = categoryRepository.findAll(PageRequest.of(0, 5));`


#### Adding Default Values
In this implementation, we define default values for pagination parameters (pageNumber and pageSize) 
using a configuration class AppConstants. 
These defaults are used when the client does not provide these parameters in the request.


The AppConstants class defines constants for default pagination values:
`
public class AppConstants {
public static final String PAGE_NUMBER = "0";  // Default page number (first page)
public static final String PAGE_Size = "50";   // Default page size (50 records per page)
}`

* PAGE_NUMBER = "0" → The default page number is 0 (zero-based index).
* PAGE_Size = "50" → The default number of items per page is 50.

### Controller Method with Default Values

`@RequestMapping(value = "public/categories", method = RequestMethod.GET)
public ResponseEntity<CategoryResponse> getAllCategories(
@RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
@RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_Size, required = false) Integer pageSize
) {
CategoryResponse categoryResponse = categoryService.getAllcategories(pageNumber, pageSize);
return new ResponseEntity<>(categoryResponse, HttpStatus.OK);
}`

###### How Default Values Work
* The @RequestParam annotation binds query parameters (pageNumber and pageSize) from the URL.
* The defaultValue attribute ensures that if these parameters are not provided in the request, 
  the values from AppConstants are used.
* required = false means that these parameters are optional.

### Implementing Sorting

--AppConstants
`public class AppConstants {
    public static final String SORT_CATEGORIES_BY ="categoryId" ;
    public static final String SORT_DIRECTION = "asc" ;
}
`
**SORT_CATEGORIES_BY**: This is the default field to sort by (e.g., "categoryId").
**SORT_DIRECTION**: This sets the default sorting direction ("asc" for ascending).
These ensure that even if the user doesn't specify sorting parameters, 
your API still returns a sorted response.


-- Controller 
`@RequestMapping(value = "public/categories",method = RequestMethod.GET)
public  ResponseEntity<CategoryResponse> getAllCategories(
@RequestParam(name="pageNumber" ,defaultValue = AppConstants.PAGE_NUMBER,required = false)Integer pageNumber,
@RequestParam(name = "pageSize",defaultValue = AppConstants.PAGE_Size,required = false) Integer pageSize,
@RequestParam(name = "sortBy",defaultValue = AppConstants.SORT_CATEGORIES_BY,required = false)     String sortBy,
@RequestParam(name = "sortOrder",defaultValue = AppConstants.SORT_DIRECTION,required = false) String sortOrder)
{
CategoryResponse categoryResponse = categoryService.getAllcategories(pageNumber,pageSize,sortBy,sortOrder);
return new ResponseEntity<>(categoryResponse,HttpStatus.OK);
}`
The controller method now accepts two query parameters: sortBy and sortOrder.
If these are not provided by the client, your defaults from AppConstants are used.


-- service
`Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
? Sort.by(sortBy).ascending()
: Sort.by(sortBy).descending();`

You dynamically build the Sort object using the provided (or default) sortBy and sortOrder.
It checks the sortOrder:
"asc" → ascending sort
"desc" → descending sort


Sort is a class provided by Spring Data JPA that helps specify sorting criteria when querying a database.
Sort.by("fieldName") → Specifies the field to sort by.
.ascending() → Sorts in ascending order (A → Z, 0 → 9).
.descending() → Sorts in descending order (Z → A, 9 → 0).

Then this sort is passed to the PageRequest:

`Pageable pageable = PageRequest.of(pageNumber, pageSize, sortByAndOrder);`
This ensures that your database query is both paginated and sorted.


## References
[Model Mapper](https://modelmapper.org/getting-started/)
