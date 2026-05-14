package uz.career.career_bot.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import uz.career.career_bot.controller.AdminController;
import uz.career.career_bot.controller.TaxonomyController;

/**
 * Exception handler for admin (Thymeleaf) pages.
 * Redirects to the previous page with a Flash message.
 */
@Slf4j
@ControllerAdvice(assignableTypes = {AdminController.class, TaxonomyController.class})
public class AdminExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public RedirectView handleNotFound(NotFoundException ex,
                                       HttpServletRequest request,
                                       RedirectAttributes ra) {
        log.warn("Admin Not found: {} | path={}", ex.getMessage(), request.getRequestURI());
        ra.addFlashAttribute("error", ex.getMessage());
        return redirectBack(request);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public RedirectView handleAlreadyExists(AlreadyExistsException ex,
                                            HttpServletRequest request,
                                            RedirectAttributes ra) {
        log.warn("Admin Already exists: {} | path={}", ex.getMessage(), request.getRequestURI());
        ra.addFlashAttribute("error", ex.getMessage());
        return redirectBack(request);
    }

    @ExceptionHandler(ValidationException.class)
    public RedirectView handleValidation(ValidationException ex,
                                         HttpServletRequest request,
                                         RedirectAttributes ra) {
        log.warn("Admin Validation: {} | path={}", ex.getMessage(), request.getRequestURI());
        ra.addFlashAttribute("error", ex.getMessage());
        return redirectBack(request);
    }

    @ExceptionHandler(BusinessException.class)
    public RedirectView handleBusiness(BusinessException ex,
                                       HttpServletRequest request,
                                       RedirectAttributes ra) {
        log.warn("Admin Business error: {} | path={}", ex.getMessage(), request.getRequestURI());
        ra.addFlashAttribute("error", ex.getMessage());
        return redirectBack(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public RedirectView handleIllegalArgument(IllegalArgumentException ex,
                                              HttpServletRequest request,
                                              RedirectAttributes ra) {
        log.warn("Admin Illegal argument: {} | path={}", ex.getMessage(), request.getRequestURI());
        ra.addFlashAttribute("error", "Incorrect information: " + ex.getMessage());
        return redirectBack(request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public RedirectView handleDataIntegrity(DataIntegrityViolationException ex,
                                            HttpServletRequest request,
                                            RedirectAttributes ra) {
        log.warn("Admin DB integrity violation at {}: {}", request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());

        String message = ex.getMostSpecificCause().getMessage();
        String userMessage;

        if (message != null && message.contains("duplicate key")) {
            userMessage = "Such an entry already exists or there is an ID conflict.";
        } else if (message != null && message.contains("foreign key")) {
            userMessage = "There is other information related to this entry, please delete it first.";
        } else {
            userMessage = "A database conflict has occurred.";
        }

        ra.addFlashAttribute("error", userMessage);
        return redirectBack(request);
    }

    @ExceptionHandler(Exception.class)
    public RedirectView handleGeneric(Exception ex,
                                      HttpServletRequest request,
                                      RedirectAttributes ra) {
        log.error("Admin Unexpected error at {}: ", request.getRequestURI(), ex);
        ra.addFlashAttribute("error", "An unexpected error occurred: " + ex.getMessage());
        return redirectBack(request);
    }

    private RedirectView redirectBack(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank() && referer.contains("/admin")) {
            return new RedirectView(referer);
        }
        return new RedirectView("/admin/dashboard");
    }
}