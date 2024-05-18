//get the base url from ENV
const baseUrl = import.meta.env.VITE_API_URL ?? "http://localhost:3001";

export const sendImage = async (image: File) => {
    //convert file to base64. Then return data from POST request
    const reader = new FileReader();
    reader.readAsDataURL(image);
    return new Promise((resolve) => {
        reader.onload = async () => {
            const base64 = reader.result;
            const response = await fetch(`${baseUrl}/upload`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({image: base64})
            });
            resolve(response);
        };
        reader.onerror = error => console.error(error);
    });
}
