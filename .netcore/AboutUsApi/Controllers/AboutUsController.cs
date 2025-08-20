using Microsoft.AspNetCore.Mvc;
using AboutUsApi.Models;
using AboutUsApi.Services;

namespace AboutUsApi.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AboutUsController : ControllerBase
    {
        private readonly EmailService _emailService;

        public AboutUsController(EmailService emailService)
        {
            _emailService = emailService;
        }

        [HttpPost]
        public async Task<IActionResult> Post([FromBody] AboutUsForm form)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            await _emailService.SendEmailAsync(form);

            return Ok(new { message = "Message sent successfully." });
        }
    }
}
